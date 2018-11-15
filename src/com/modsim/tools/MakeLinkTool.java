package com.modsim.tools;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import com.modsim.Main;
import com.modsim.operations.CreateOperation;
import com.modsim.res.Colors;
import com.modsim.util.*;
import com.modsim.gui.view.ViewUtil;
import com.modsim.modules.*;
import com.modsim.modules.parts.Port;

/**
 * Module linking tool
 * @author aw12700
 *
 */
public class MakeLinkTool extends BaseTool {

	public Port source;

	public Vec2 start = new Vec2();
	public Vec2 current = new Vec2();
	public Path curve;

	public boolean working = false;

	@Override
	public BaseTool lbDown(int x, int y, boolean isShiftDown) {
		// Get the clicked port, start linking from here
		if (!working) {
			if (!startLink(x, y, isShiftDown)) {
				return null;
			}
		}
		// End / place new control point
		else {
			if (endLink(x, y)) {
				return null;
			}
		}

		return this;
	}

	@Override
	public BaseTool mouseMove(int x, int y) {
		if (working) {
			current = ViewUtil.screenToWorld(new Vec2(x, y), false);

			Port p = ViewUtil.screenSpace_portAt(x, y);
			if (p != null) {
				curve.setEnd(p);
			}
			else {
				curve.setEnd(current);
			}
		}

		return this;
	}
	@Override
	public BaseTool mouseDrag(int x, int y) {
		return mouseMove(x, y);
	}

	@Override
	public BaseTool keyDown(int key) {
		if (   key == KeyEvent.VK_BACK_SPACE
            || key == KeyEvent.VK_DELETE
            || key == KeyEvent.VK_X) {
			if (curve.removePt()) 	return this;
			else 					return null;
		}

		return this;
	}

	@Override
	public void paintWorld(Graphics2D g) {
		if (working) {
			g.setColor(Colors.selectedLink);
			g.setStroke(new BasicStroke(2));
			curve.draw(g);
		}
	}

	/**
	 * Attempts to start a new link
	 * @param x X position to check
	 * @param y Y position to check
	 * @return True if successful
	 */
	public boolean startLink(int x, int y, boolean isShiftDown) {
		source = ViewUtil.screenSpace_portAt(x, y);
		if (source != null) {
            Main.selection.clear();

            working = true;
			start = new Vec2(x, y);
            if (isShiftDown) {
	             curve = new BezierPath();
            } else {
                curve = new StraightPath();
            }

			curve.setEnd(source.getDisplayPosW());
			curve.setStart(source);

			return true;
		}

		return false;
	}

	/**
	 * Attempts to complete the link. Adds a control point if unsuccessful.
	 * @param x X position to check
	 * @param y Y position to check
	 * @return True if link was completed
	 */
	public boolean endLink(int x, int y) {
		Port targ = ViewUtil.screenSpace_portAt(x, y);

		if (targ != null) {
			Main.opStack.beginCompoundOp();
			Link l = Link.createLink(source, targ, curve);
			if (l != null) {
	            Main.sim.addLink(l);
	            Main.sim.propagate(l.targ.owner);
				Main.opStack.pushOp(new CreateOperation(l));
			}
			Main.opStack.endCompoundOp();
			working = false;
			return true;
		}
		else {
			curve.addPt(new CtrlPt(current));
			return false;
		}
	}

	@Override
	public void cancel() {

	}
}
