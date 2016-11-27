package com.modsim.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import com.modsim.Main;
import com.modsim.gui.view.View;
import com.modsim.tools.PlaceTool;
import com.modsim.modules.BaseModule;
import static com.modsim.modules.BaseModule.AvailableModules;

public class ModuleIcon extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	public BaseModule module = null;
	public String text;
	public boolean hlt = false;

	/**
	 * Creates a module selection button
	 * @param t Text to display
	 * @param m Module to display (and duplicate for placement)
	 */
        @Deprecated
	public ModuleIcon(String t, BaseModule m) {
		text = t;
		module = m;
		setPreferredSize(new Dimension(180, 60));
		addMouseListener(this);
	}

        /**
	 * Creates a module selection button
	 * @param def The module definition to back this button with
	 */
	public ModuleIcon(AvailableModules def) {
		text = def.toString();
		module = def.getSrcModule();
		setPreferredSize(new Dimension(180, 60));
		addMouseListener(this);
	}

	@Override
	public void paintComponent(Graphics oldG) {
		Graphics2D g = (Graphics2D) oldG;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color highlight, back;
		if (Main.ui.compPane.selected == this) {
			highlight = new Color(200,200,255);
			back = new Color(240, 240, 255);
		}
		else if (hlt) {
			highlight = new Color(230,230,230);
			back = Color.white;
		}
		else {
			highlight = new Color(210,210,210);
			back = Color.white;
		}

		GradientPaint paint = new GradientPaint(0, 0, back, 0, 20, highlight);
		g.setPaint(paint);
		g.fillRect(0, 0, getWidth(), getHeight());
		paint = new GradientPaint(0, getHeight()-40, highlight, 0, getHeight(), back);
		g.setPaint(paint);
		g.fillRect(0, getHeight()-40, getWidth(), getHeight());

		g.setColor(new Color(200,200,200));
		g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

		g.setColor(Color.WHITE);
		g.drawLine(0, 1, getWidth(), 1);

		g.setColor(Color.BLACK);
		g.drawString(text, 20, getHeight() / 2 + 6);

		g.translate(getWidth() - 40, getHeight() / 2);
		double scale = 90 / (module.h + module.w);
		g.scale(scale, scale);
		module.paintStatic(g);
		module.paintDynamic(g);
	}

	public void mousePressed(MouseEvent arg0) {
		// Select
		Main.ui.compPane.selected = this;

		// Generate placement tool
		View v = Main.ui.view;
		if (v.curTool != null) v.curTool.cancel();
		v.curTool = new PlaceTool(module.createNew());

		// Redraw UI
		Main.ui.compPane.repaint();
	}

	public void mouseEntered(MouseEvent arg0) {
		hlt = true;
		Main.ui.compPane.repaint();
	}

	public void mouseExited(MouseEvent arg0) {
		hlt = false;
		Main.ui.compPane.repaint();
	}

	public void mouseClicked(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}


}
