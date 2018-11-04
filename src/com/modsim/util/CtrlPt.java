package com.modsim.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.modsim.Main;
import com.modsim.simulator.PickableEntity;
import com.modsim.operations.DeleteOperation;

public class CtrlPt extends PickableEntity {

    public Path parent = null;

    /**
     * Create a control point at the specified position
     * @param x
     * @param y
     */
    public CtrlPt(double x, double y) {
        pos.set(x, y);
    }

    /**
     * Create a control point at the specified position
     * @param p
     */
    public CtrlPt(Vec2 p) {
        pos.set(p);
    }

    /**
     * Create a copy of the specified control point
     * @param pt
     */
    public CtrlPt(CtrlPt pt) {
        pos.set(pt.pos);
    }

    /**
     * Updates the parent path upon movement
     * @param newPos
     */
    @Override
    public void onMove(Vec2 newPos) {
        if (parent != null) parent.updateContours();
    }

    @Override
    public boolean intersects(Vec2 pt) {
        return pos.dist(pt) < 15;
    }

    @Override
    public int getType() {
        return PickableEntity.CTRLPT;
    }

    /**
     * Draws the point
     * @param g
     */
    public void draw(Graphics2D g) {
        Color oldC = g.getColor();
        g.setColor(Color.WHITE);
        g.fillOval((int)pos.x - 3, (int)pos.y - 3, 6, 6);
        g.setColor(oldC);
        g.drawOval((int)pos.x - 3, (int)pos.y - 3, 6, 6);
    }

    @Override
    public void drawBounds(Graphics2D g) {
        Color c = g.getColor();
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2));
        g.drawRect((int)(pos.x - 5), (int)(pos.y - 5), 10, 10);
        g.setColor(c);
    }

    @Override
    public boolean within(double x, double y, double x2, double y2) {
        if (    x < pos.x && x2 > pos.x &&
                y < pos.y && y2 > pos.y )
            return true;
        else return false;
    }

    @Override
    public void delete() {
        DeleteOperation deleteOp = new DeleteOperation(this, parent.ctrlPts.indexOf(this));
        parent.removePt(this);
        Main.opStack.pushOp(deleteOp);
    }

    @Override
    public PickableEntity createNew() {
        return new CtrlPt(this);
    }

}
