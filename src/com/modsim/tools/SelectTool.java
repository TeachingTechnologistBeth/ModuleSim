package com.modsim.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.modsim.gui.view.ViewUtil;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.Selection;
import com.modsim.util.Vec2;

public class SelectTool extends BaseTool {

    private Vec2 screenDragStart = new Vec2();
    private Vec2 dragStart = new Vec2();
    private Vec2 dragPos = new Vec2();

    private boolean dragging = false;

    private Selection mySelection = null;
    private Selection oldSelection = null;

    private PickableEntity pickedEntity = null;

    public SelectTool() {
        mySelection = new Selection(Main.selection);
        oldSelection = new Selection(Main.selection);
    }

    @Override
    public BaseTool lbUp(int x, int y) {
        PickableEntity e = ViewUtil.screenSpace_entityAt(x, y);

        if (dragging && !mySelection.isEmpty()) {
            // Set the final selection
            Main.selection.set(mySelection);

            // If we're doing additive selection, add in the old selection as well
            if (BaseTool.SHIFT) {
                Main.selection.addAll(oldSelection);
            }

            return null;
        }
        else if (e != null) {
            if (BaseTool.SHIFT)
                Main.selection.toggle(e);
            else {
                // Single-object selection on click
                Main.selection.set(e);
            }
        }
        else {
            // Clear the selection if we click on an empty spot AND we're not holding shift
            if (!BaseTool.SHIFT) Main.selection.clear();
        }

        return null;
    }

    // This just fixes the weird delayed-start drag bug
    @Override
    public BaseTool lbDown(int x, int y, boolean isShiftDown) {
        synchronized (this) {
            // Don't actually set dragging=true till we get a mouseDrag() event
            screenDragStart.set(x, y);
            dragStart.set(ViewUtil.screenToWorld(screenDragStart, true));

            // Need to base contextual action on the item at the START of the drag
            pickedEntity = ViewUtil.screenSpace_entityAt(x, y);
        }

        return this;
    }

    @Override
    public BaseTool mouseDrag(int x, int y) {
        if (!dragging) {
            // Move if we started the drag on an entity
            if (pickedEntity != null) {
                if (!pickedEntity.selected) {
                    if (BaseTool.SHIFT) {
                        Main.selection.add(pickedEntity);
                    }
                    else {
                        Main.selection.set(pickedEntity);
                    }
                }
                return new MoveTool((int) screenDragStart.x, (int) screenDragStart.y);
            }
            // Otherwise we'll start a selection box
            else {
                synchronized (this) {
                    dragging = true;

                    // N.B. dragStart is already set thanks to lbDown() handler
                    dragPos.set(ViewUtil.screenToWorld(new Vec2(x, y), true));
                }
            }
        }

        // Selection box logic
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
        dragPos.set(ViewUtil.screenToWorld(new Vec2(x, y), true));

        Vec2 delta = new Vec2(dragPos);
        delta.sub(dragStart);

        double rx = Math.min(dragStart.x, dragStart.x + delta.x);
        double rx2 = Math.max(dragStart.x, dragStart.x + delta.x);
        double ry = Math.min(dragStart.y, dragStart.y + delta.y);
        double ry2 = Math.max(dragStart.y, dragStart.y + delta.y);

        mySelection.set(ViewUtil.worldSpace_entitiesWithin(rx, ry, rx2, ry2));
        Main.selection.set(mySelection);
        if (BaseTool.SHIFT) Main.selection.addAll(oldSelection);

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
