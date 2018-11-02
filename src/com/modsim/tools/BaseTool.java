package com.modsim.tools;

import java.awt.Graphics2D;

/**
 * Base for tool classes
 * @author aw12700
 *
 */
public abstract class BaseTool {
	public static boolean SHIFT = false;
	public static boolean CTRL = false;

	// Conditional handling
	public boolean handlesRbDown()			{return false;}
	public boolean handlesRbUp()			{return false;}

	public BaseTool rbDown(int x, int y)  	{return this;}
	public BaseTool rbUp(int x, int y)  	{return this;}

	public BaseTool lbDown(int x, int y, boolean isShiftDown) 	{return this;}
	public BaseTool lbUp(int x, int y) 		{return this;}
	public BaseTool mouseMove(int x, int y) {return this;}
	public BaseTool mouseDrag(int x, int y) {return this;}
	public BaseTool keyDown(int key) {return this;}
	public BaseTool keyUp(int key) {return this;}

	// All operations must be cancellable
	public abstract void cancel();

	public void paintWorld(Graphics2D g) {}
	public void paintScreen(Graphics2D g) {}
}
