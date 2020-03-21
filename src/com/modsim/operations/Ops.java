package com.modsim.operations;

import com.modsim.Main;
import com.modsim.gui.HelpWindow;
import com.modsim.gui.view.ViewUtil;
import com.modsim.modules.BaseModule;
import com.modsim.simulator.PickableEntity;
import com.modsim.tools.PlaceTool;
import com.modsim.util.XMLReader;
import com.modsim.util.XMLWriter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;

/**
 * Created by awick on 22/09/2015.
 * Consolidates various design operations into ActionListener event handlers
 */
public class Ops {

	public static DesignAction showHelp = new DesignAction(event -> {new HelpWindow();}, "Show help");
	
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

        /**
         * Begins editing on a new design (with confirm if the current file is unsaved)
         */
        public static void fileNew() {
            if (Main.ui.checkSave()) {
                Main.sim.newSim();
            }
        }
    }

    // Core application actions
    public static final DesignAction
            undo, redo,
            copy, paste, delete,
            rotateCW, rotateCCW, rotate180,
            toggleSnap,
            labelEdit, labelBig, labelSmall,
            pause, run, step, toggleRun, zoomIn, zoomOut, resetView, toggleAA, open, save, saveAs, fileNew, quit;

    static {
        // Keyboard shortcuts
        KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlL = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlShiftS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);

        KeyStroke space = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        KeyStroke period = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0);
        KeyStroke del = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke lBracket = KeyStroke.getKeyStroke('[');
        KeyStroke rBracket = KeyStroke.getKeyStroke(']');


        /// Action implementations

        // Undo stack
        undo = new DesignAction(event -> {
            Main.ui.view.cancelTool();
            Main.opStack.undo();
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Undo", "Undo the previous operation", ctrlZ);
        redo = new DesignAction(event -> {
            if (!Main.ui.view.hasTool()) {
                Main.opStack.redo();
            }
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Redo", "Repeat an undone operation", ctrlY);

        // Copy/paste
        copy = new DesignAction(event -> Main.clipboard.copy(Main.selection), "Copy",
                "Copy selection to application clipboard", ctrlC);
        paste = new DesignAction(event -> {
            Main.ui.view.cancelTool();

            if (!Main.clipboard.isEmpty()) {
                try {
                    Main.ui.view.setTool(new PlaceTool(Main.clipboard));
                }
                catch (Exception e) {
                    // Clipboard contents may not be valid
                    System.out.println(e.getMessage());
                }
            }
            else {
                System.out.println("Nothing to paste");
            }
        }, "Paste", "Paste into the design from the application clipboard", ctrlV);

        // Deletion
        delete = new DesignAction(event -> {
            Main.selection.deleteAll();
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Delete", "Deletes the selection", del);

        // Rotation
        rotateCW = new DesignAction(event -> doRotate(BaseModule.rotationDir.ROT_CW),
                "Rotate clockwise", null, rBracket);
        rotateCCW = new DesignAction(event -> doRotate(BaseModule.rotationDir.ROT_CCW),
                "Rotate counter-clockwise", null, lBracket);
        rotate180 = new DesignAction(event -> doRotate(BaseModule.rotationDir.ROT_180),
                "Rotate 180");

        toggleSnap = new DesignAction(event -> ViewUtil.snap = !ViewUtil.snap, "Toggle Snap");

        // Label editing
        labelEdit = new DesignAction(event -> {
            String labelStr = JOptionPane.showInputDialog(Main.ui.frame, "");
            if (labelStr == null) return;

            Main.opStack.beginCompoundOp();
            for (PickableEntity entity : Main.selection.getEntities()) {
                if (entity.getType() == PickableEntity.MODULE) {
                    BaseModule module = (BaseModule) entity;
                    Main.opStack.pushOp(new LabelOperation(module, module.label, labelStr));
                    module.label = labelStr;
                }
            }
            Main.opStack.endCompoundOp();
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Add/Edit label", null, ctrlL);

        // Label sizing
        labelBig = new DesignAction(event -> {
            Main.opStack.beginCompoundOp();
            for (PickableEntity entity : Main.selection.getEntities()) {
                if (entity.getType() == PickableEntity.MODULE) {
                    BaseModule m = (BaseModule) entity;
                    LabelSizeOperation sizeOp = new LabelSizeOperation(m, m.labelSize, 1);
                    m.labelSize = 1;
                    Main.opStack.pushOp(sizeOp);
                }
            }
            Main.opStack.endCompoundOp();
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Big");
        labelSmall = new DesignAction(event -> {
            Main.opStack.beginCompoundOp();
            for (PickableEntity entity : Main.selection.getEntities()) {
                if (entity.getType() == PickableEntity.MODULE) {
                    BaseModule m = (BaseModule) entity;
                    LabelSizeOperation sizeOp = new LabelSizeOperation(m, m.labelSize, 0);
                    m.labelSize = 0;
                    Main.opStack.pushOp(sizeOp);
                }
            }
            Main.opStack.endCompoundOp();
            //redraw
            Main.ui.view.flagStaticRedraw();
        }, "Small (default)");

        // Simulator controls
        pause = new DesignAction(event -> Main.sim.stop(), "Pause Simulation");
        run = new DesignAction(event -> Main.sim.start(), "Run Simulation");
        toggleRun = new DesignAction(event -> {
            if (Main.sim.running) Main.sim.stop();
            else Main.sim.start();
        }, "Run/Pause Simulation","Toggles the running state of the simulation", space);
        step = new DesignAction(event -> {
            Main.sim.stop();
            Main.sim.step();
        }, "Step Simulation", "Steps the simulation forward by one iteration", period);
        
        //Zoom controls
        zoomIn = new DesignAction(event -> Main.ui.zoomInToView(), "Zoom In");
        zoomOut = new DesignAction(event -> Main.ui.zoomOutToView(), "Zoom Out");
        resetView = new DesignAction(event -> Main.ui.resetView(), "Reset View");
        
        // View controls
        toggleAA = new DesignAction(event -> {
            Main.ui.view.useAA = !Main.ui.view.useAA;
            //redraw
            Main.ui.view.flagStaticRedraw();
        },
                "Toggle anti-aliasing", "Toggles anti-aliased rendering in the viewport: " +
                "disabling AA may improve performance on older machines.");

        // FileIO operations
        open = new DesignAction(event -> FileIO.open(),
                "Open", "Open a saved design (discards the current one)", ctrlO, KeyEvent.VK_O);
        save = new DesignAction(event -> FileIO.save(),
                "Save", "Save the current design", ctrlS, KeyEvent.VK_S);
        saveAs = new DesignAction(event -> FileIO.saveAs(),
                "Save As", "Save the current design in a specific location", ctrlShiftS);
        fileNew = new DesignAction(event -> FileIO.fileNew(),
                "New Design", "Start editing a new design (discard the current one)", ctrlN, KeyEvent.VK_N);

        quit = new DesignAction(event -> {if (Main.ui.checkSave()) Main.ui.frame.dispose(); },
                "Quit", "Exit ModuleSim", KeyEvent.VK_Q);
    }

    private static void doRotate(BaseModule.rotationDir dir){
        Main.opStack.beginCompoundOp();
        for (PickableEntity entity : Main.selection.getEntities()) {
            if (entity.getType() == PickableEntity.MODULE) {
                BaseModule m = (BaseModule) entity;
                m.rotate(dir);
                Main.opStack.pushOp(new RotateOperation(m, dir));
            }
        }
        Main.opStack.endCompoundOp();
        //redraw
        Main.ui.view.flagStaticRedraw();
    }

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
