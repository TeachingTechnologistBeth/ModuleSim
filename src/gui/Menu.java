package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;

import javax.swing.*;

import modules.BaseModule;
import simulator.Main;
import util.XMLReader;
import util.XMLWriter;

/**
 * Manager for the main window app_menu
 * @author aw12700
 *
 */
public class Menu {

    private final JMenuBar app_menu;
    private final FilenameFilter simFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".modsim");
        }
    };

    /**
     * Retrieve the jmenu
     */
    public JMenuBar getJMenuBar() {
        return app_menu;
    }

    /**
     * Create the app_menu
     */
    public Menu() {
        app_menu = new JMenuBar();
        addFileMenu();
        addEditMenu();
        addViewMenu();
        addSimMenu();
    }

    private void saveAs() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
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
        }
    }

    private void addFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);


        // Open simulation (with native dialog box)
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        menuItem.setToolTipText("Open a saved simulation (discards the current one)");

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences prefs = Preferences.userNodeForPackage(this.getClass());
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

                        if (fd.getFile() == null) return;
                        path = fd.getDirectory() + fd.getFile();
                    }

                    File file = new File(path);
                    XMLReader.readFile(file);
                    prefs.put("sim_fileDir", fd.getDirectory());
                }
            }
        });
        fileMenu.add(menuItem);

        // Save simulation (native dialog box)
        menuItem = new JMenuItem("Save");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        menuItem.setToolTipText("Save the current simulation");

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String curPath = Main.sim.filePath;

                if (curPath.isEmpty()) {
                    saveAs();
                } else {
                    XMLWriter.writeFile(new File(curPath));
                }
            }
        });
        fileMenu.add(menuItem);

        // Save as
        menuItem = new JMenuItem("Save As");
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        menuItem.setAccelerator(key);
        menuItem.setToolTipText("Save the current simulation");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        menuItem.setToolTipText("Start editing a new simulation (discard the current one)");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.sim.newSim();
                Main.ui.view.camX = 0;
                Main.ui.view.camY = 0;
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_E);
        menuItem.setToolTipText("Exit the application");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(menuItem);

        app_menu.add(fileMenu);
    }

    private void addEditMenu() {
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);

        // Copy
        JMenuItem item = new JMenuItem("Copy");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.ui.view.copy(Main.ui.view.selection);
            }
        });
        edit.add(item);

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        item.setAccelerator(key);

        // Paste
        item = new JMenuItem("Paste");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.ui.view.paste();
            }
        });
        edit.add(item);

        key = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        item.setAccelerator(key);

        // Undo
        item = new JMenuItem("Undo");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.ui.view.undo();
            }
        });
        edit.add(item);

        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        item.setAccelerator(key);

        // Redo
        item = new JMenuItem("Redo");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.ui.view.redo();
            }
        });
        edit.add(item);

        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        item.setAccelerator(key);

        app_menu.add(edit);
    }

    private void addSimMenu() {
        JMenu sim = new JMenu("Simulation");
        sim.setMnemonic(KeyEvent.VK_S);

        JMenuItem menuItem = new JMenuItem("Run/Pause");
        menuItem.setToolTipText("Toggles the running state of the simulation");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Main.sim.running) {
                    Main.sim.stop();
                } else {
                    Main.sim.start();
                }
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        sim.add(menuItem);

        menuItem = new JMenuItem("Step");
        menuItem.setToolTipText("Steps, then pauses the simulation");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.sim.stop();
                Main.sim.step();
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
        sim.add(menuItem);


        app_menu.add(sim);
    }

    private void addViewMenu() {
        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);

        JMenuItem menuItem = new JMenuItem("Toggle AA");
        menuItem.setToolTipText("Toggles high-quality rendering on/off");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.ui.view.quality = !Main.ui.view.quality;
            }
        });
        view.add(menuItem);

        app_menu.add(view);
    }

}
