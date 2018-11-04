package com.modsim.modules.parts;

import java.awt.Graphics2D;
import com.modsim.modules.BaseModule;

/**
 * Base class for visible components on the com.modsim.modules -
 * LEDs, switches, screens and the like.
 * @author aw12700
 *
 */
public abstract class VisiblePart {

	public enum RefreshMode {
		Static,
		Dynamic
	}

	int x, y;
	public BaseModule owner;

	// Interaction
	public boolean lbDown(int x, int y, boolean isShiftDown) {return false;}
	public boolean lbUp(int x, int y) {return false;}

	// Display
	public abstract void paint(Graphics2D g);
	public abstract RefreshMode getRefreshMode();

	/***
	 * Use to update the part's persistence-of-vision data
	 */
	public abstract void povTick();
}
