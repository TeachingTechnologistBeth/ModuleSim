package com.modsim.util;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class StraightLine {

	public Vec2 p1 = new Vec2();
	public Vec2 p2 = new Vec2();

	public Line2D line;

	/**
	 * Create a new bezier line
	 */
	public StraightLine(Vec2 pt1, Vec2 pt2) {
		p1.set(pt1);
		p2.set(pt2);

		line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
	}

	public StraightLine() {
		line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * Update the display line
	 */
	public void update() {
		line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * Calculate a point on the line
	 * @param t Point on line to calculate
	 * @return Cartesian coordinates of point
	 */
	public Vec2 calcPoint(double t) {
        Vec2 a = new Vec2();
        t = t * (p1.dist(p2));

        if (p2.x - p1.x == 0) {
            a.set(p1.x, p1.y + (p2.y - p1.y < 0 ? -t : t));
        }
        else {
            double m = (p2.y - p1.y) / (p2.x - p1.x);
            a.set(p1.x + (p2.x - p1.x < 0 ? -t : t), p1.y + (m * (p2.x - p1.x < 0 ? -t : t)));
        }
        
		return a;
	}

	/**
	 * Draw the line using java's straight line
	 */
	public void draw(Graphics2D g) {
		g.draw(line);
	}

	/**
	 * Return distance between start and end point
	 */
	public double len() {
		Vec2 d = new Vec2(p1);
		d.sub(p2);
		return d.len();
	}
}
