package com.modsim;

import com.modsim.modules.BaseModule;
import com.modsim.operations.LabelOperation;
import com.modsim.operations.RotateOperation;
import com.modsim.simulator.PickableEntity;
import com.modsim.tools.PlaceTool;
import com.modsim.util.XMLReader;
import com.modsim.util.XMLWriter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.security.InvalidParameterException;
import java.util.prefs.Preferences;

/**
 * Created by awick on 22/09/2015.
 * Consolidates various design operations into ActionListener event handlers
 */
public class Ops {

    public static class FileIO {
        private static final FilenameFilter simFileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".modsim");
            }
        };

        /**
         * Offers to save the current document with a save dialog.
         * @return True if document was saved
         */
        public static boolean saveAs() {
            Preferences prefs = Preferences.userNodeForPackage(FileIO.class);
            FileDialog fd = new FileDialog((java.awt.Frame) null, "Save File", FileDialog.SAVE);
            fd.setFilenameFilter(simFileFilter);
            // (?) can just append .modsim if the user doesn't
            if (Main.sim.filePath.isEmpty()) {
                fd.setFile("*.modsim");
            }
            else {
                int ind = Main.sim.filePath.lastIndexOf('/');
                fd.setFile(Main.sim.filePath.substring(ind + 1));
            }

            fd.setDirectory(prefs.get("sim_fileDir", ""));
            fd.setVisible(true);

            if (fd.getFile() != null) {
                String path = fd.getDirectory() + fd.getFile();

                // Is the file being created with the correct extension?
                if (!path.endsWith(".modsim")) {
                    path = path + ".modsim";
                }

                XMLWriter.writeFile(new File(path));
                Main.opStack.resetModified();
                return true;
            }

            return false;
        }

        /**
         * Saves the current file (calls saveAs() if the document has not yet been saved to the filesystem)
         * @return True if the document was saved
         */
        public static boolean save() {
            String curPath = Main.sim.filePath;

            if (curPath.isEmpty()) {
                return saveAs();
            } else {
                XMLWriter.writeFile(new File(curPath));
                Main.opStack.resetModified();
                return true;
            }
        }

        /**
         * Design-file opening
         * @return True if a .modsim file was loaded
         */
        public static boolean open() {
            if (!Main.ui.checkSave()) return false;

            Preferences prefs = Preferences.userNodeForPackage(FileIO.class);
            FileDialog fd = new FileDialog((java.awt.Frame) null, "Open File", FileDialog.LOAD);

            fd.setFilenameFilter(simFileFilter);
            fd.setFile("*.modsim"); // FilenameFilter doesn't work on Windows

            fd.setDirectory(prefs.get("sim_fileDir", ""));
            fd.setVisible(true);

            if (fd.getFile() != null) {
                String path = fd.getDirectory() + fd.getFile();

                // Loop till we get a valid input
                while (!path.endsWith(".modsim")) {
                    JOptionPane.showMessageDialog(null, "That doesn't appear to be a ModuleSim file.");
                    fd.setFile("*.modsim");
                    fd.setVisible(true);

                    if (fd.getFile() == null) return false;
                    path = fd.getDirectory() + fd.getFile();
                }

                File file = new File(path);
                XMLReader.readFile(file);
                prefs.put("sim_fileDir", fd.getDirectory());
                return true;
            }

            return false;
        }
    }

    /**
     * Enumeration of available commands
     */
    public enum Command {
        UNDO("CMD_UNDO", "Undo", "Undo the previous operation"),
        REDO("CMD_REDO", "Redo", "Repeat an operation that was undone"),
        COPY("CMD_COPY", "Copy", "Copy the selection to the application clipboard"),
        PASTE("CMD_PASTE", "Paste", "Paste into the design from the application clipboard"),
        DELETE("CMD_DELETE", "Delete", "Deletes the selection"),

        ROTATE_CW("CMD_ROT_CW", "Rotate clockwise"),
        ROTATE_CCW("CMD_ROT_CCW", "Rotate counter-clockwise"),
        ROTATE_180("CMD_ROT_180", "Rotate 180"),

        EDIT_LABEL("CMD_LABEL_EDIT", "Add/Edit Label"),
        LABEL_BIG("CMD_LABEL_BIG", "Big"),
        LABEL_SMALL("CMD_LABEL_SMALL", "Small (default)"),

        PAUSE("SIM_PAUSE", "Pause Simulation"),
        RUN("SIM_RUN", "Run Simulation"),
        STEP("SIM_STEP", "Step Simulation"),
        RUN_TOGGLE("SIM_TOGGLE", "Run/Pause Simulation", "Toggles the running state of the simulation"),

        AA_TOGGLE("VIEW_TOGGLE_AA", "Toggle anti-aliasing", "Toggles anti-aliased rendering in the viewport: " +
                "disabling AA may improve performance on older machines."),

        OPEN("FILE_OPEN", "Open", "Open a saved design (discards the current one)", KeyEvent.VK_O),
        SAVE("FILE_SAVE", "Save", "Save the current design", KeyEvent.VK_S),
        SAVE_AS("FILE_SAVE_AS", "Save As", "Save the current design in a specific location"),
        NEW("FILE_NEW", "New Design", "Start editing a new design (discard the current one)", KeyEvent.VK_N),

        QUIT("APP_EXIT", "Quit", "Exit ModuleSim", KeyEvent.VK_Q);

        String cmdStr, name, tooltip = null;
        int mnemonic = -1;

        JMenuItem myItem = null;

        public String str() { return cmdStr; }
        public String getName() { return name; }
        public String getToolTip() { return tooltip; }

        public int getMnemonic() { return mnemonic; }

        /**
         * Generates or returns a menu item for this command.
         * @return This command's menu item
         */
        public JMenuItem generateMenuItem() {
            return generateMenuItem(null);
        }

        /**
         * Generates or returns a menu item for this command. Setting the shortcut will override any previous
         * setting.
         * @param shortcut Keyboard shortcut for the command
         * @return This command's menu item
         */
        public JMenuItem generateMenuItem(KeyStroke shortcut) {
            JMenuItem newItem = new JMenuItem(name);
            newItem.addActionListener(Ops.core);
            newItem.setActionCommand(cmdStr);

            if (tooltip != null) {
                newItem.setToolTipText(tooltip);
            }
            if (mnemonic != -1) {
                newItem.setMnemonic(mnemonic);
            }

            if (shortcut != null) {
                newItem.setAccelerator(shortcut);
                if (myItem != null) myItem.setAccelerator(shortcut);
            }
            else if (myItem != null) {
                newItem.setAccelerator(myItem.getAccelerator());
            }

            myItem = newItem;
            return newItem;
        }

        Command(String cmdStr, String name) {
            this.cmdStr = cmdStr;
            this.name = name;
        }

        Command(String cmdStr, String name, String tooltip) {
            this(cmdStr, name);
            this.tooltip = tooltip;
        }

        Command(String cmdStr, String name, String tooltip, int mnemonic) {
            this(cmdStr, name, tooltip);
            this.mnemonic = mnemonic;
        }

        public static Command fromCmdStr(String text) {
            if (text != null) {
                for (Command cmd : Command.values()) {
                    if (text.equals(cmd.cmdStr)) {
                        return cmd;
                    }
                }
            }

            throw new IllegalArgumentException("Command '" + text + "' not found");
        }
    }

    /**
     * Main handler for menu button-type input events
     */
    public static final ActionListener core = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmdStr = e.getActionCommand();
            Command cmd = Command.fromCmdStr(cmdStr);

            switch (cmd) {
                // General commands
                case UNDO:
                    Main.ui.view.cancelTool();
                    Main.opStack.undo();
                    break;

                case REDO:
                    if (!Main.ui.view.hasTool()) Main.opStack.redo();
                    break;

                case COPY:
                    Main.clipboard.copy(Main.selection);
                    break;

                case PASTE:
                    Main.ui.view.cancelTool();

                    if (!Main.clipboard.isEmpty()) {
                        Main.ui.view.setTool(new PlaceTool(Main.clipboard));
                    }
                    else {
                        System.out.println("Nothing to paste");
                    }
                    break;

                case DELETE:
                    Main.selection.deleteAll();
                    break;

                // Rotation
                case ROTATE_CW:
                case ROTATE_CCW:
                case ROTATE_180:
                    BaseModule.rotationDir dir;
                    if (cmdStr.contains("CCW")) {
                        dir = BaseModule.rotationDir.ROT_CCW;
                    }
                    else if (cmdStr.contains("CW")) {
                        dir = BaseModule.rotationDir.ROT_CW;
                    }
                    else if (cmdStr.contains("180")) {
                        dir = BaseModule.rotationDir.ROT_180;
                    }
                    else {
                        throw new InvalidParameterException(cmd + " is not a valid rotation command.");
                    }

                    Main.opStack.beginCompoundOp();
                    for (PickableEntity entity : Main.selection.getEntities()) {
                        if (entity.getType() == PickableEntity.MODULE) {
                            BaseModule m = (BaseModule) entity;
                            m.rotate(dir);
                            Main.opStack.pushOp(new RotateOperation(m, dir));
                        }
                    }
                    Main.opStack.endCompoundOp();
                    break;

                // Labelling
                case EDIT_LABEL:
                    String labelStr = JOptionPane.showInputDialog(Main.ui.frame, "");
                    if (labelStr == null) break;

                    Main.opStack.beginCompoundOp();
                    for (PickableEntity entity : Main.selection.getEntities()) {
                        if (entity.getType() == PickableEntity.MODULE) {
                            BaseModule module = (BaseModule) entity;
                            Main.opStack.pushOp(new LabelOperation(module, module.label, labelStr));
                            module.label = labelStr;
                        }
                    }
                    Main.opStack.endCompoundOp();
                    break;

                case LABEL_BIG:
                    for (PickableEntity entity : Main.selection.getEntities()) {
                        if (entity.getType() == PickableEntity.MODULE) {
                            ((BaseModule) entity).labelSize = 1;
                        }
                    }
                    break;
                case LABEL_SMALL:
                    for (PickableEntity entity : Main.selection.getEntities()) {
                        if (entity.getType() == PickableEntity.MODULE) {
                            ((BaseModule) entity).labelSize = 0;
                        }
                    }
                    break;

                // Simulator commands
                case PAUSE:
                    Main.sim.stop();
                    break;
                case RUN:
                    Main.sim.start();
                    break;
                case STEP:
                    Main.sim.stop();
                    Main.sim.step();
                    break;
                case RUN_TOGGLE:
                    if (Main.sim.running) Main.sim.stop();
                    else Main.sim.start();
                    break;

                // View options
                case AA_TOGGLE:
                    Main.ui.view.useAA = !Main.ui.view.useAA;
                    break;

                // File IO
                case OPEN:
                    FileIO.open();
                    break;
                case SAVE:
                    FileIO.save();
                    break;
                case SAVE_AS:
                    FileIO.saveAs();
                    break;
                case NEW:
                    if (Main.ui.checkSave()) {
                        Main.sim.newSim();
                    }
                    break;


                // Quit
                case QUIT:
                    if (Main.ui.checkSave()) {
                        System.exit(0);
                    }
                    break;


                default:
                    throw new IllegalArgumentException("Unrecognized command enumeration");
            }
        }
    };

    /**
     * Handler for adjusting the simulation speed by slider
     */
    public static final ChangeListener sliderSetSpeed = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            // Adjust sim speed
            JSlider src = (JSlider) e.getSource();
            int val = src.getValue();
            long delay = (long) Math.pow(1.35, 60 - val);
            com.modsim.simulator.Sim.delay = delay -1;
        }
    };

}
