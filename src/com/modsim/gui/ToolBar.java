package com.modsim.gui;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import com.modsim.Ops;
import com.modsim.res.ResourceLoader;

public class ToolBar {
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
		slideSpeed.addChangeListener(Ops.sliderSetSpeed);
	}

	/**
	 * Generate the toolbar buttons
	 */
	private void addButtons() {
		btnPause = new JButton(Ops.pause);
		btnPause.setHideActionText(true);
		toolbar.add(btnPause);

		btnRun = new JButton(Ops.run);
		btnRun.setHideActionText(true);
		toolbar.add(btnRun);

		btnStep = new JButton(Ops.step);
		btnStep.setHideActionText(true);
		toolbar.add(btnStep);
	}

	/**
	 * Creates a toolbar button
	 * @param img Image name (PNG)
	 * @param command Command to call
	 * @return The new toolbar button
	 */
	private JButton createButton(String img) {
		// Get the image
		String path = img + ".png";
		URL imgURL = ResourceLoader.class.getResource(path);

		JButton btn = new JButton();
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
}
