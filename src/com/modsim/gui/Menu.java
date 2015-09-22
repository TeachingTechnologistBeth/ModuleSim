package com.modsim.gui;

import java.awt.event.KeyEvent;
import javax.swing.*;
import com.modsim.Ops;

/**
 * Manager for the main window app_menu
 * @author aw12700
 *
 */
public class Menu {

    private final JMenuBar app_menu;

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

    private void addFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        // Open simulation (with native dialog box)
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        fileMenu.add(Ops.Command.OPEN.generateMenuItem(key));

        // Save simulation (native dialog box)
        key = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        fileMenu.add(Ops.Command.SAVE.generateMenuItem(key));

        // Save as
        key = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        fileMenu.add(Ops.Command.SAVE_AS.generateMenuItem(key));

        // New file
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        fileMenu.add(Ops.Command.NEW.generateMenuItem(key));

        // Quit
        fileMenu.addSeparator();
        fileMenu.add(Ops.Command.QUIT.generateMenuItem());

        app_menu.add(fileMenu);
    }

    private void addEditMenu() {
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);

        // Rotation
        KeyStroke key = KeyStroke.getKeyStroke('[');
        edit.add(Ops.Command.ROTATE_CCW.generateMenuItem(key));
        key = KeyStroke.getKeyStroke(']');
        edit.add(Ops.Command.ROTATE_CW.generateMenuItem(key));
        edit.add(Ops.Command.ROTATE_180.generateMenuItem());

        // Copy
        key = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        edit.add(Ops.Command.COPY.generateMenuItem(key));

        // Paste
        key = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        edit.add(Ops.Command.PASTE.generateMenuItem(key));

        // Undo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        edit.add(Ops.Command.UNDO.generateMenuItem(key));

        // Redo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        edit.add(Ops.Command.REDO.generateMenuItem(key));

        edit.addSeparator();

        key = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        edit.add(Ops.Command.EDIT_LABEL.generateMenuItem(key));

        app_menu.add(edit);
    }

    private void addSimMenu() {
        JMenu sim = new JMenu("Simulation");
        sim.setMnemonic(KeyEvent.VK_S);

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        sim.add(Ops.Command.RUN_TOGGLE.generateMenuItem(key));

        key = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0);
        sim.add(Ops.Command.STEP.generateMenuItem(key));

        app_menu.add(sim);
    }

    private void addViewMenu() {
        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.add(Ops.Command.AA_TOGGLE.generateMenuItem());
        app_menu.add(view);
    }

}
