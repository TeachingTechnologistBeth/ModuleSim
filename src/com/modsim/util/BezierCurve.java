package com.modsim.util;

import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

public class BezierCurve {

	public Vec2 p1 = new Vec2();
	public Vec2 p2 = new Vec2();

	public Vec2 c1 = new Vec2();
	public Vec2 c2 = new Vec2();

	public CubicCurve2D curve;

	/**
	 * Create a new bezier curve
	 */
	public BezierCurve(Vec2 pt1, Vec2 pt2, Vec2 ct1, Vec2 ct2) {
		p1.set(pt1);
		p2.set(pt2);
		c1.set(ct1);
		c2.set(ct2);

		curve = new CubicCurve2D.Double(p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y);
	}

	public BezierCurve() {
		curve = new CubicCurve2D.Double(p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y);
	}

	/**
	 * Update the display curve
	 */
	public void update() {
		curve = new CubicCurve2D.Double(p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y);
	}

	/**
	 * Calculate a point on the curve
	 * @param t Point on curve to calculate
	 * @return Cartesian coordinates of point
	 */
	public Vec2 calcPoint(double t) {
		Vec2 a = new Vec2();
		Vec2 b = new Vec2();

		double u 	= 1.0 - t;
		double tt 	= t*t;
		double uu 	= u*u;
		double uuu 	= uu*u;
		double ttt 	= tt*t;

		a.set(p1);
		a.mul(uuu);

		b.set(c1);
		b.mul(3*uu*t);
		a.add(b);

		b.set(c2);
		b.mul(3*u*tt);
		a.add(b);

		b.set(p2);
		b.mul(ttt);
		a.add(b);

		return a;
	}

	/**
	 * Draw the curve using java's cubic curve
	 */
	public void draw(Graphics2D g) {
		g.draw(curve);
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
