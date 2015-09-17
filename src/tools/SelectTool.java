package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import gui.ViewUtil;
import simulator.Main;
import simulator.PickableEntity;
import util.Vec2;

public class SelectTool extends BaseTool {

    private Vec2 dragStart = new Vec2();
    private Vec2 dragPos = new Vec2();
    private boolean dragging = false;

    private List<PickableEntity> mySelection = null;

    @Override
    public BaseTool lbUp(int x, int y) {
        PickableEntity e = ViewUtil.entityAt(x, y);

        if (dragging) {
            // If we're not doing additive, clear any existing selection
            if (!BaseTool.SHIFT && mySelection != null) {
                Main.ui.view.clearSelect();
                Main.ui.view.select(mySelection);
            }

            return null;
        }
        else if (e != null) {
            if (BaseTool.SHIFT)
                Main.ui.view.toggleSelect(e);
            else {
                // Single-object selection on click
                Main.ui.view.clearSelect();
                Main.ui.view.select(e);
            }
        }
        else {
            // Clear the selection if we click on an isEmpty spot
            Main.ui.view.clearSelect();
        }

        return null;
    }

    // This just fixes the weird delayed-start drag bug
    @Override
    public BaseTool lbDown(int x, int y) {
        synchronized (this) {
            // Don't actually set dragging=true till we get a mouseDrag() event
            dragStart.set(ViewUtil.screenToWorld(new Vec2(x, y)));
        }

        return this;
    }

    @Override
    public BaseTool mouseDrag(int x, int y) {
        // Select & start a move operation
        PickableEntity e = ViewUtil.entityAt(x, y);

        if (!dragging) {
            if (e != null && Main.ui.view.selection.contains(e)) {
                return new MoveTool(x, y);
            }
            else {
                synchronized (this) {
                    dragging = true;

                    // N.B. dragStart is already set thanks to lbDown() handler
                    dragPos.set(ViewUtil.screenToWorld(new Vec2(x, y)));
                }
            }
        }
        else {
            if (BaseTool.CTRL) {
                // Switch to move tool
                return new MoveTool(x, y);
            }

            // Move the camera if we're dragging near the edge
            double moveSpeed = 5;
            double edgeWidth = 15;

            if (x < edgeWidth) {
                Main.ui.view.camX += moveSpeed;
                Main.ui.view.calcXForm();
            }
            else if (x > Main.ui.view.getWidth() - edgeWidth) {
                Main.ui.view.camX -= moveSpeed;
            }

            if (y < edgeWidth) {
                Main.ui.view.camY += moveSpeed;
                Main.ui.view.calcXForm();
            }
            else if (y > Main.ui.view.getHeight() - edgeWidth) {
                Main.ui.view.camY -= moveSpeed;
            }

            //System.out.println("More dragging " + x + ", " + y);
            dragPos.set(ViewUtil.screenToWorld(new Vec2(x, y)));

            Vec2 delta = new Vec2(dragPos);
            delta.sub(dragStart);

            double rx = Math.min(dragStart.x, dragStart.x + delta.x);
            double rx2 = Math.max(dragStart.x, dragStart.x + delta.x);
            double ry = Math.min(dragStart.y, dragStart.y + delta.y);
            double ry2 = Math.max(dragStart.y, dragStart.y + delta.y);

            mySelection = ViewUtil.worldSpace_entitiesWithin(rx, ry, rx2, ry2);
            Main.ui.view.select(mySelection);
        }

        return this;
    }

    @Override
    public void paintScreen(Graphics2D g) {
        synchronized (this) {
            if (dragging) {
                Vec2 screen_dragStart = ViewUtil.worldToScreen(dragStart);
                Vec2 screen_dragPos = ViewUtil.worldToScreen(dragPos);
                Vec2 delta = new Vec2(screen_dragPos);
                delta.sub(screen_dragStart);

                int x = (int) Math.min(screen_dragStart.x, screen_dragStart.x + delta.x);
                int y = (int) Math.min(screen_dragStart.y, screen_dragStart.y + delta.y);

                g.setColor(Color.BLUE);
                g.setStroke(new BasicStroke(1));
                g.drawRect(x, y, (int)Math.abs(delta.x), (int)Math.abs(delta.y));
            }
        }
    }

    @Override
    public void cancel() {
        synchronized (this) {
            dragging = false;
        }
    }
}
