package com.modsim.simulator;

import java.awt.Graphics2D;

import com.modsim.util.Vec2;

public abstract class PickableEntity {

    public static int MODULE = 0;
    public static int CTRLPT = 1;

    public Vec2 tempPos = new Vec2();
    public Vec2 pos = new Vec2();
    public boolean selected = false;
    public boolean enabled = false;

    /**
     * Test whether the object intersects a given point (in world space)
     * @param pt Point to test against
     * @return True if the object intersects the point
     */
    public abstract boolean intersects(Vec2 pt);

    /**
     * Test whether the object is within a given area (in world space)
     * @return True if the object is contained within the rectangular area
     */
    public abstract boolean within(double x, double y, double x2, double y2);

    public abstract int getType();

    /**
     * Move to the specified position
     * @param newPos
     */
    public final void move(Vec2 newPos) {
        pos.set(newPos);
        onMove(newPos);
    }

    /**
     * Event provided for implementing classes to respond to movement
     * @param newPos
     */
    public void onMove(Vec2 newPos) {}

    /**
     * Move relative to the stored temporary position
     * @param delta
     */
    public final void moveRelative(Vec2 delta) {
        Vec2 newPos = new Vec2(tempPos);
        newPos.add(delta);
        move(newPos);
    }

    public abstract void drawBounds(Graphics2D g);

    public abstract void delete();

    public abstract PickableEntity createNew();
}
