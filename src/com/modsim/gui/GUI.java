package com.modsim.gui;

import com.modsim.operations.Ops;
import com.modsim.gui.view.ContextMenu;
import com.modsim.gui.view.View;
import com.modsim.res.ResourceLoader;
import com.modsim.Main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.*;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public class GUI {

	public JFrame frame = null;
	public Container pane = null;
	//public JSplitPane vSplit = null;
	public JSplitPane hSplit = null;
	public View view = null;
	public Menu menu = null;
	public ToolBar toolbar = null;

	public ContextMenu popup = null;

	public ComponentPane compPane;
	//public JPanel bottom;

	/**
	 * Allows the user to save or cancel if work may be lost
	 * @return Whether to complete the file operation
	 */
	public boolean checkSave() {
		if (Main.opStack.isModified()) {
			int res = JOptionPane.showConfirmDialog(frame, "Would you like to save first?", "Unsaved changes",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			switch (res) {
				case JOptionPane.YES_OPTION:
					return Ops.FileIO.save();
				case JOptionPane.NO_OPTION:
					return true;

				default:
					// Take no action
					System.out.println("File operation aborted");
					return false;
			}
		}

		return true;
	}

	public void generateUI() {
		preConfig();
		createFrame();
		createCompPane();
		createViewport();
		createMenu();
		createToolbar();
		createContextMenu();
	}

    /**
	 * Display / hide the UI
	 * @param arg Whether or not to display
	 */
	public void showUI(boolean arg) {
		if (arg) {
			Preferences prefs = Preferences.userNodeForPackage(GUI.class);
			if (prefs.getBoolean("window_stored", false)) {
				// Window border has been stored
				int windowX, windowY, windowWidth, windowHeight;
				windowX = prefs.getInt("window_x", 0);
				windowY = prefs.getInt("window_y", 0);
				windowHeight = prefs.getInt("window_height", 600);
				windowWidth = prefs.getInt("window_width", 800);
				// Check the window is within the boundaries of the active display(s)
                GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                boolean onDesktop = false;
                for (int i = 0; i < devices.length; i++) {
                    Rectangle displayBounds = devices[i].getDefaultConfiguration().getBounds();
                    if (displayBounds.intersects(windowX, windowY, windowWidth, windowHeight)) {
                        onDesktop = true;
                    }
                }
                if (onDesktop) {
                    frame.setBounds(windowX, windowY, windowWidth, windowHeight);
                }
                else {
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                }
				// Restores the maximise state
				if (prefs.getBoolean("window_maximised", false)) {
					frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				}
			}
			else {
				// Pack & center the window
				frame.pack();
				frame.setLocationRelativeTo(null);
			}
		}
		frame.setVisible(arg);
	}

    /**
     * Updates the window title to "ModuleSim - /path/to/file.modsim"
     */
	public void updateTitle() {
        if (Main.sim.filePath.isEmpty()) {
            frame.setTitle("ModuleSim - Unsaved Simulation");
        }
        else {
            frame.setTitle("ModuleSim - " + Main.sim.filePath);
        }
    }

	/**
	 * UI Configuration
	 */
	private void preConfig() {
		// Attempt to set the Look and Feel to nimbus
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the main frame and layout panels
	 */
	private void createFrame() {
		frame = new JFrame();
        updateTitle();

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
				// Prevents closing without saving first
                if (checkSave()) {
					// Store window size/maximise state
					Preferences prefs = Preferences.userNodeForPackage(GUI.class);
					if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
						prefs.putBoolean("window_maximised", true);
						Rectangle windowBounds = e.getWindow().getBounds();
						prefs.putInt("window_x", windowBounds.x + 50);
						prefs.putInt("window_y", windowBounds.y + 50);
					}
					else {
						prefs.putBoolean("window_maximised", false);
						Rectangle windowBounds = e.getWindow().getBounds();
						prefs.putInt("window_x", windowBounds.x);
						prefs.putInt("window_y", windowBounds.y);
						prefs.putInt("window_width", windowBounds.width);
						prefs.putInt("window_height", windowBounds.height);
					}
					prefs.putBoolean("window_stored", true);
					// Close the window
                    e.getWindow().dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

		pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());

		hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pane.add(hSplit, BorderLayout.CENTER);

		// Set the icon
        java.util.List<Image> iconList = new ArrayList<>();
        URL iconUrl = ResourceLoader.class.getResource("icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        iconList.add(kit.createImage(iconUrl));

        iconUrl = ResourceLoader.class.getResource("icon64.png");
        iconList.add(kit.createImage(iconUrl));

        iconUrl = ResourceLoader.class.getResource("icon32.png");
        iconList.add(kit.createImage(iconUrl));

        iconUrl = ResourceLoader.class.getResource("icon16.png");
        iconList.add(kit.createImage(iconUrl));

        frame.setIconImages(iconList);
	}

	/**
	 * Fills in the menu
	 */
	private void createMenu() {
		menu = new Menu();
		frame.setJMenuBar(menu.getJMenuBar());
	}

	/**
	 * Creates the toolbar
	 */
	private void createToolbar() {
		toolbar = new ToolBar();
		pane.add(toolbar.getJToolBar(), BorderLayout.NORTH);
	}

	/**
	 * Creates the component pane
	 */
	private void createCompPane() {
		JPanel wrapper = new JPanel(new BorderLayout());
		compPane = new ComponentPane();

		wrapper.add(compPane, BorderLayout.CENTER);
		JScrollPane sp = new JScrollPane(	wrapper,
											JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.getVerticalScrollBar().setUnitIncrement(10);
		sp.setPreferredSize(new Dimension(210,0));
		sp.setMinimumSize(new Dimension(210, 60));

		hSplit.add(sp, JSplitPane.LEFT);
	}

	/**
	 * Creates the main viewport
	 */
	private void createViewport() {
		view = new View();
		view.setPreferredSize(new Dimension(800, 600));

		hSplit.add(view, JSplitPane.RIGHT);
	}

	/*
	 * Accessible method for zooming into view
	 */
	public void zoomInToView()
	{
		view.zoom(view.getWidth()/2, view.getHeight()/2, -2.0);
	}
	
	/*
	 * Accessible method for zooming out of view
	 */
	public void zoomOutToView()
	{
		view.zoom(view.getWidth()/2, view.getHeight()/2, 2.0);
	}
	
	public void resetView()
	{
		view.resetView();
	} 
	
	
	/**
	 * Creates the context menu
	 */
	private void createContextMenu() {
		popup = new ContextMenu();
	}

	/**
     * Generates the memory viewer/editor dialog
     */
    public MemEdit newMemEdit() {
        return new MemEdit();
    }
}
