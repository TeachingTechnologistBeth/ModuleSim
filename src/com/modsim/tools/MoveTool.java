package com.modsim.tools;

import com.modsim.gui.view.ViewUtil;
import com.modsim.Main;
import com.modsim.operations.MoveOperation;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.Vec2;

import java.util.List;

public class MoveTool extends BaseTool {

	private Vec2 startPt;
	private List<PickableEntity> entities;

	/**
	 * Start a move operation. Assumes a selection exists in the View.
	 */
	public MoveTool(int x, int y) {
		startPt = ViewUtil.screenToWorld(new Vec2(x,y), false);
		entities = Main.selection.getEntities();

		for (PickableEntity e : entities) {
			e.enabled = false;
			e.tempPos.set(e.pos);
		}
	}

	@Override
	public BaseTool mouseDrag(int x, int y) {
		Vec2 p = ViewUtil.screenToWorld(new Vec2(x, y), false);
        p.sub(startPt);

        for (PickableEntity e : entities) {
			e.moveRelative(p);
		}

		return this; // still moving
	}

	@Override
	public BaseTool lbUp(int x, int y) {
		Vec2 p = ViewUtil.screenToWorld(new Vec2(x, y), false);
		p.sub(startPt);

		// We're done - store the operation
		Main.opStack.beginCompoundOp();
		for (PickableEntity e : entities) {
			e.enabled = true;
            e.moveRelative(p);
			e.tempPos.set(0, 0);

			Main.opStack.pushOp(new MoveOperation(e, p));
		}
		Main.opStack.endCompoundOp();

		return null; // operation completed
	}

	@Override
	public void cancel() {
        // Reset entity positions
        for (PickableEntity e : entities) {
            e.enabled = true;
            e.moveRelative(new Vec2(0, 0));
        }
    }

}
