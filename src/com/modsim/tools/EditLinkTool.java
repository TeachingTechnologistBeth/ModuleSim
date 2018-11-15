package com.modsim.tools;

import com.modsim.Main;
import com.modsim.gui.view.ViewUtil;
import com.modsim.modules.Link;
import com.modsim.operations.CreateOperation;
import com.modsim.operations.MoveOperation;
import com.modsim.res.Colors;
import com.modsim.util.Path.PointInfo;
import com.modsim.util.CtrlPt;
import com.modsim.util.Vec2;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;

/**
 * Created by awick on 21/09/2015.
 *
 * Tool for modifying existing links
 */
public class EditLinkTool extends BaseTool {

    private Link link;
    private PointInfo nearbyInfo = null;
    private Vec2 editPoint = null;

    private CtrlPt placePoint = null;
    private Vec2 moveStart = new Vec2();

    public EditLinkTool(Link link) {
        this.link = link;
        link.highlight = true;

        // Treat the edit operation atomically
        Main.opStack.beginCompoundOp();
    }

    @Override
    public BaseTool mouseMove(int x, int y) {
        Vec2 worldPoint = ViewUtil.screenToWorld(new Vec2(x, y), false);

        CtrlPt pickCtrl = link.path.closestCtrlPt(worldPoint);
        nearbyInfo = link.path.approxClosestPoint(worldPoint, 10);

        if (pickCtrl != null && pickCtrl.pos.dist(worldPoint) < 25) {
            editPoint = new Vec2(pickCtrl.pos);
        }
        else if (nearbyInfo.pt.dist(worldPoint) < 20) {
            editPoint = new Vec2(nearbyInfo.pt);
        }
        else {
            editPoint = null;
        }

        return this;
    }

    @Override
    public boolean handlesRbDown() {
        return true;
    }

    @Override
    public BaseTool rbDown(int x, int y) {
        if (placePoint != null) return this; // do nothing if we're placing a point

        Vec2 worldPoint = ViewUtil.screenToWorld(new Vec2(x, y), false);
        CtrlPt pickCtrl = link.path.closestCtrlPt(worldPoint);

        if (pickCtrl != null && pickCtrl.pos.dist(worldPoint) < 25) {
            // Delete the control point!
            pickCtrl.delete();
        }

        return this;
    }

    @Override
    public BaseTool lbDown(int x, int y, boolean isShiftDown) {
        // Try picking nearest control point
        Vec2 worldPoint = ViewUtil.screenToWorld(new Vec2(x, y), false);
        CtrlPt pickCtrl = link.path.closestCtrlPt(worldPoint);

        if (pickCtrl != null && pickCtrl.pos.dist(worldPoint) < 25) {
            placePoint = pickCtrl;
            moveStart.set(pickCtrl.pos);
        }
        else if (nearbyInfo.pt.dist(worldPoint) < 20) {
            // Create control point
            placePoint = new CtrlPt(editPoint);
            link.path.addPt(nearbyInfo.curveIndex, placePoint);
            Main.sim.addEntity(placePoint);

            // Register operation
            moveStart.set(editPoint);
            Main.opStack.pushOp(new CreateOperation(placePoint, nearbyInfo.curveIndex));
        }
        else {
            // Quit out if the user clicks away
            Main.opStack.endCompoundOp();
            link.highlight = false;
            return null;
        }

        return this;
    }

    @Override
    public BaseTool mouseDrag(int x, int y) {
        if (placePoint != null) {
            Vec2 worldPoint = ViewUtil.screenToWorld(new Vec2(x, y), false);
            placePoint.pos.set(worldPoint);
            editPoint.set(placePoint.pos);
            link.path.calcCurves();
        }

        return this;
    }

    @Override
    public BaseTool lbUp(int x, int y) {
        if (placePoint != null) {
            Vec2 delta = new Vec2(placePoint.pos);
            delta.sub(moveStart);
            Main.opStack.pushOp(new MoveOperation(placePoint, delta));

            // Done
            placePoint = null;
        }

        return this;
    }

    @Override
    public BaseTool keyDown(int key) {
        switch (key) {
            case KeyEvent.VK_X:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                Main.opStack.cancelCompoundOp();
                link.highlight = false;
                link.delete();
                return null;

            case KeyEvent.VK_ENTER:
                // Done
                Main.opStack.endCompoundOp();
                link.highlight = false;
                return null;
        }

        return this;
    }

    @Override
    public void cancel() {
        Main.opStack.cancelCompoundOp();
        link.highlight = false;
    }

    @Override
    public void paintWorld(Graphics2D g) {
        if (editPoint != null) {
            g.setColor(Colors.selectedLink);
            g.fill(new Arc2D.Double(editPoint.x - 5, editPoint.y - 5, 10, 10, 0, 360, Arc2D.CHORD));
        }
    }
}
