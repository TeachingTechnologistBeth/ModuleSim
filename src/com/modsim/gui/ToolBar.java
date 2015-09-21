package com.modsim.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.modsim.res.ResourceLoader;
import com.modsim.Main;
import com.modsim.simulator.Sim;

public class ToolBar implements ActionListener, ChangeListener {

	public JToolBar toolbar = null;
	public JButton btnPause, btnRun, btnStep;
	public JSlider slideSpeed;

	/**
	 * Retrieve usable toolbar object
	 */
	public JToolBar getJToolBar() {
		return toolbar;
	}

	/**
	 * Creates the toolbar
	 */
	public ToolBar() {
		toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// Buttons
		toolbar.addSeparator(new Dimension(30, 0));
		addButtons();

		// Slider
		toolbar.addSeparator(new Dimension(30, 0));
		JLabel lbl = new JLabel("Speed: ");
		toolbar.add(lbl);
		slideSpeed = new JSlider(JSlider.HORIZONTAL, 0, 60, 10);
		toolbar.add(slideSpeed);
		slideSpeed.addChangeListener(this);
	}

	/**
	 * Generate the toolbar buttons
	 */
	private void addButtons() {
		btnPause = createButton("pause", "PAUSE_SIM", "Pause the simulator");
		toolbar.add(btnPause);

		btnRun = createButton("run", "RUN_SIM", "Start the simulator");
		toolbar.add(btnRun);

		btnStep = createButton("step", "STEP_SIM", "Step the simulation");
		toolbar.add(btnStep);
	}

	/**
	 * Creates a toolbar button
	 * @param img Image name (PNG)
	 * @param actionCmd Command name
	 * @param toolTip Tool-tip text to display
	 * @return The new toolbar button
	 */
	private JButton createButton(	String img,
									String actionCmd,
									String toolTip ) {
		// Get the image
		String path = img + ".png";
		URL imgURL = ResourceLoader.class.getResource(path);

		JButton btn = new JButton();
		btn.setActionCommand(actionCmd);
		btn.setToolTipText(toolTip);
		btn.addActionListener(this);
		btn.setFocusable(false);

		// Try the icon
		if (imgURL != null) {
			btn.setIcon(new ImageIcon(imgURL, null));
			btn.setOpaque(false);
		}
		else {
			btn.setText("IMAGE MISSING");
			System.err.println("Image not found: \"" + path + "\"");
		}

		return btn;
	}

	public void actionPerformed(ActionEvent e) {
		// Handle toolbar actions
		String cmd = e.getActionCommand();
		if (cmd.equals(btnPause.getActionCommand())) {
			Main.sim.stop();
		}
		else if (cmd.equals(btnRun.getActionCommand())) {
			Main.sim.start();
		}
		else if (cmd.equals(btnStep.getActionCommand())) {
		    Main.sim.stop();
		    Main.sim.step();
		}
	}

    public void stateChanged(ChangeEvent e) {
        // Adjust sim speed
        JSlider src = (JSlider) e.getSource();
        int val = (int) src.getValue();
        long delay = (long) Math.pow(1.35, 60 - val);
        Sim.delay = delay -1;
    }
}
