package tools;

import gui.ViewUtil;
import simulator.Main;
import simulator.PickableEntity;
import util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class MoveTool extends BaseTool {

	private Vec2 startPt;
	private List<PickableEntity> entities;

	/**
	 * Start a move operation. Assumes a selection exists in the View.
	 */
	public MoveTool(int x, int y) {
		startPt = ViewUtil.screenToWorld(new Vec2(x,y));
		entities = new ArrayList<PickableEntity>(Main.ui.view.selection);

		for (PickableEntity e : entities) {
			e.enabled = false;
			e.tempPos.set(e.pos);
		}
	}
	
	@Override
	public BaseTool mouseDrag(int x, int y) {
		Vec2 p = ViewUtil.screenToWorld(new Vec2(x, y));
        p.sub(startPt);

        for (PickableEntity e : entities) {
			e.moveRelative(p);
		}
		
		return this; // still moving
	}
	
	@Override
	public BaseTool lbUp(int x, int y) {
		Vec2 p = ViewUtil.screenToWorld(new Vec2(x, y));
		p.sub(startPt);

		// We're done - store the operation
		Main.ui.view.opStack.beginCompoundOp();
		for (PickableEntity e : entities) {
			e.enabled = true;
            e.moveRelative(p);
			e.tempPos.set(0, 0);

			Main.ui.view.opStack.pushOp(new MoveOperation(e, p));
		}
		Main.ui.view.opStack.endCompoundOp();
		
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
