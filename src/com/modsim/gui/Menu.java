package com.modsim.gui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.*;
import com.modsim.operations.Ops;

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
        JMenuItem help = new JMenuItem(Ops.showHelp);
        help.setMaximumSize(new Dimension(80, 100));
        help.setMnemonic(KeyEvent.VK_H);
        app_menu.add(help);
    }

    private void addFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        fileMenu.add(new JMenuItem(Ops.open));
        fileMenu.add(new JMenuItem(Ops.save));
        fileMenu.add(new JMenuItem(Ops.saveAs));
        fileMenu.add(new JMenuItem(Ops.fileNew));

        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(Ops.quit));

        app_menu.add(fileMenu);
    }

    private void addEditMenu() {
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);

        edit.add(new JMenuItem(Ops.undo));
        edit.add(new JMenuItem(Ops.redo));
        edit.add(new JMenuItem(Ops.copy));
        edit.add(new JMenuItem(Ops.paste));
        edit.addSeparator();
        edit.add(new JMenuItem(Ops.rotateCW));
        edit.add(new JMenuItem(Ops.rotateCCW));
        edit.add(new JMenuItem(Ops.rotate180));
        edit.addSeparator();
        edit.add(new JMenuItem(Ops.toggleSnap));
        edit.addSeparator();
        edit.add(new JMenuItem(Ops.labelEdit));

        app_menu.add(edit);
    }

    private void addSimMenu() {
        JMenu sim = new JMenu("Simulation");
        sim.setMnemonic(KeyEvent.VK_S);

        sim.add(Ops.toggleRun);
        sim.add(Ops.step);

        app_menu.add(sim);
    }

    private void addViewMenu() {
        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.add(Ops.toggleAA);
        view.add(Ops.resetView);
        app_menu.add(view);
    }

}
