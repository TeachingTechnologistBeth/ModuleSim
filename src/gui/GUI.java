package gui;

import simulator.Main;

import java.awt.*;

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
		// Pack, centre and show
		frame.pack();
		frame.setLocationRelativeTo(null);
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());
		
		//vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//pane.add(vSplit, BorderLayout.CENTER);
		
		hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pane.add(hSplit, BorderLayout.CENTER);
		//vSplit.add(hSplit, JSplitPane.TOP);
		
		/*bottom = new JPanel();
		bottom.setPreferredSize(new Dimension(100, 150));
		
		JScrollPane sp = new JScrollPane(	bottom, 
											JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		vSplit.add(sp, JSplitPane.BOTTOM);
		vSplit.setResizeWeight(1.0);*/
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
