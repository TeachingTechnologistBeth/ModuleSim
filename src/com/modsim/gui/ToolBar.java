package com.modsim.gui;

import java.awt.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import com.modsim.operations.Ops;

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

		Insets margin = new Insets(5, 10, 5, 20);
		toolbar.setMargin(margin);

		// Buttons
		addButtons();
		toolbar.addSeparator(new Dimension(15, 0));

		// Slider
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
}
