package com.modsim.util;

import java.awt.geom.Point2D;

/**
 * 2D Vector class
 * @author aw12700
 *
 */
public class Vec2 extends Point2D {

	public double x, y;

	/**
	 * Equality test
	 * @param v Vector to test against
	 * @return True if the vectors are equivalent
	 */
	public boolean equals(Vec2 v) {
		return (x == v.x && y == v.y);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * Zero vector
	 */
	public Vec2() {
		x = 0;
		y = 0;
	}

	/**
	 * Duplicate an existing vector
	 */
	public Vec2(Vec2 v) {
		x = v.x;
		y = v.y;
	}

	/**
	 * Create a vector from an array
	 */
	public Vec2(double[] pt) {
		x = pt[0];
		y = pt[1];
	}

	/**
	 * New vector with given x/y values
	 */
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Retrieve vector as double array
	 */
	public double[] asArray() {
		double[] a = {x, y};
		return a;
	}

	/**
	 * Sets to match given vector
	 * @param v vector to copy
	 */
	public void set(Vec2 v) {
		x = v.x;
		y = v.y;
	}

	/**
	 * Sets to given x/y values
	 * @param x
	 * @param y
	 */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Return the length squared of the vector
	 */
	public double lenSq() {
		return x*x + y*y;
	}

	/**
	 * Return the length of the vector
	 */
	public double len() {
		return Math.sqrt(lenSq());
	}

	/**
	 * Set the length of a non-zero vector
	 * @param l Length to match
	 */
	public void setLen(double l) {
		if (x == 0 && y == 0) return;
		double curL = len();
		mul(l / curL);
	}

	/**
	 * Multiplies by a scalar
	 * @param s Scalar value
	 */
	public void mul(double s) {
		x *= s;
		y *= s;
	}

	/**
	 * Multiplies by another vector
	 * @param v Vector to multiply by
	 */
	public void mul(Vec2 v) {
		x *= v.x;
		y *= v.y;
	}

	/**
	 * Adds another vector to this
	 * @param v Vector to add
	 */
	public void add(Vec2 v) {
		x += v.x;
		y += v.y;
	}

	/**
	 * Adds a scalar to this
	 * @param s Scalar to add
	 */
	public void add(double s) {
		x += s;
		y += s;
	}

	/**
	 * Subtracts another vector from this
	 * @param v Vector to subtract
	 */
	public void sub(Vec2 v) {
		x -= v.x;
		y -= v.y;
	}

	/**
	 * Subtracts a scalar from this
	 * @param s Scalar to subtract
	 */
	public void sub(double s) {
		x -= s;
		y -= s;
	}

	/**
	 * Negates the vector
	 */
	public void neg() {
		x = -x;
		y = -y;
	}

	/**
	 * Normalizes the vector (set to unit length).
	 * Does nothing if the vector is 0.
	 */
	public void norm() {
		if (x == 0 && y == 0) return;
		mul(1.0 / len());
	}

	/**
	 * Sets to normalized version of given vector
	 * @param v Vector to normalize
	 */
	public void norm(Vec2 v) {
		x = v.x;
		y = v.y;
	}

	/**
	 * Calculates the dot (scalar) product of this an another vector
	 * @param v Other vector
	 * @returns The scalar product of the two vectors
	 */
	public double dot(Vec2 v) {
		return x*v.x + y*v.y;
	}

	/**
	 * Calculates the angle between this and another vector.
	 * Returns 0 if either vector is zero-length.
	 * @param v Other vector
	 * @return The angle (in radians) to the other vector
	 */
	public double angle(Vec2 v) {
		if ((x == 0 && y == 0) || (v.x == 0 && v.y == 0)) {
			return 0;
		}

		double dot = dot(v);
		double mag = len() * v.len();

		return Math.acos(dot/mag);
	}

	/**
	 * Calculates the distance between this and another vector,
	 * as if between two points.
	 * @param v Other vector
	 * @return The distance
	 */
	public double dist(Vec2 v) {
		Vec2 temp = new Vec2(this);
		temp.sub(v);
		return temp.len();
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setLocation(double x, double y) {
		set(x, y);
	}


}
