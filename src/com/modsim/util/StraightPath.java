package com.modsim.util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.modsim.modules.parts.Port;
import com.modsim.Main;

/**
 * Manages a path consisting of connected bezier lines
 * @author aw12700
 *
 */
public class StraightPath extends Path {

	public List<StraightLine> lines = new ArrayList<>();

	public PointInfo approxClosestPoint(Vec2 searchPt, int iterations) {
		Vec2 bestPoint = new Vec2();
		double bestDist = Double.POSITIVE_INFINITY;
        double bestT = 0;
        int bestcurveIndex = -1;

		for (int cInd = 0; cInd < lines.size(); cInd++) {
            StraightLine line = lines.get(cInd);
			double topT = 1.0;
			double bottomT = 0.0;

			for (int i = 0; i < iterations; i++) {
				double t1 = bottomT + (topT - bottomT) / 4;
				double t2 = topT - (topT - bottomT) / 4;
				Vec2 p1 = line.calcPoint(t1);
				Vec2 p2 = line.calcPoint(t2);

				if (p1.dist(searchPt) < p2.dist(searchPt)) {
					topT = (topT + bottomT) / 2;
				}
				else {
					bottomT = (topT + bottomT) / 2;
				}
			}

			Vec2 linePoint = line.calcPoint((topT + bottomT) / 2);
			double lineDist = linePoint.dist(searchPt);
			if (lineDist < bestDist) {
				bestDist = lineDist;
				bestPoint = linePoint;
                bestcurveIndex = cInd;
                bestT = (topT + bottomT) / 2;
			}
		}

        PointInfo pInfo = new PointInfo();
        pInfo.pt = bestPoint;
        pInfo.dist = bestDist;
        pInfo.curveIndex = bestcurveIndex;
        pInfo.t = bestT;
		return pInfo;
	}

    public CtrlPt closestCtrlPt(Vec2 searchPt) {
        double bestDist = Double.POSITIVE_INFINITY;
        CtrlPt bestPt = null;

        for (CtrlPt pt : ctrlPts) {
            if (pt.pos.dist(searchPt) < bestDist) {
                bestPt = pt;
                bestDist = pt.pos.dist(searchPt);
            }
        }

        return bestPt;
    }

	/**
	 * Defines an initial path
	 * @param start The start-point
	 * @param c1off Offset for the first control point (from start)
	 * @param end The end-point
	 * @param c2off Offset for the second control point (from end)
	 */
	public StraightPath(Vec2 start, Vec2 end) {
		lines.add(new StraightLine(start, end));
	}

	/**
	 * Creates an "isEmpty" path
	 */
	public StraightPath() {
		lines.add(new StraightLine(new Vec2(), new Vec2()));
	}

	/**
	 * Creates a duplicate path
	 * @param line
	 */
	public StraightPath(StraightPath line) {
	    this();

        for (CtrlPt c : line.ctrlPts) {
            addPt((CtrlPt) c.createNew());
        }
        calcCurves();
    }

    public Path duplicate() {
        return new StraightPath(this);
    }

    /**
	 * Moves the startpoint
	 */
	public void setStart(Vec2 end) {
		StraightLine first = lines.get(0);
		first.p1 = new Vec2(end);
		first.update();
	}
	public void setStart(Vec2 end, Vec2 c) {
        setStart(end);
    }
	public void setStart(Port p) {
		setStart(p.getDisplayPosW());
	}

	/**
	 * Moves the endpoint
	 */
	public void setEnd(Vec2 end) {
		StraightLine last = lines.get(lines.size() - 1);
		last.p2 = new Vec2(end);
		updateContours();
	}
	public void setEnd(Vec2 end, Vec2 c) {
        setEnd(end);
    }
	public void setEnd(Port p) {
		setEnd(p.getDisplayPosW());
	}

	/**
	 * Updates the path list
	 */
	public void calcCurves() {
		Vec2 start = lines.get(0).p1;
		Vec2 end = lines.get(lines.size() - 1).p2;

		lines.clear();

		// First path - start pt and first ctrl pt
		lines.add(new StraightLine());
		setStart(start);

		// Iterate the control points
		for (int i = 0; i < ctrlPts.size(); i++) {
			Vec2 pt = ctrlPts.get(i).pos;

			// Update existing path
			lines.get(i).p2 = pt;

			// Add next path
			StraightLine c = new StraightLine();
			c.p1 = pt;
			lines.add(c);
		}

		// Set end
		setEnd(end);

		// Update the contours
		updateContours();
	}

	/**
	 * Updates the (intermediate) lines to new control points
	 */
	public void updateContours() {
		for (int i = 0; i < lines.size(); i++) {
			StraightLine last2 = null;
			if (i - 1 >= 0) {
				last2 = lines.get(i - 1);
			}
			StraightLine last = lines.get(i);

			Vec2 compPt;
			if (last2 != null) {
				compPt = last2.p1;
			}
			else {
				compPt = last.p1;
			}

			last.update();
			if (last2 != null) last2.update();
		}
	}

	/**
	 * Adds a control point
	 */
	public void addPt(CtrlPt pt) {
	    pt.parent = this;
		ctrlPts.add(pt);
		calcCurves();
	}

	/**
	 * Adds a control point at the specified position
	 * @param index Index to add point at. Must be < ctrlPts.size()
	 * @param pt The control point to add
	 */
	public void addPt(int index, CtrlPt pt) {
		pt.parent = this;
		if (index <= ctrlPts.size()) {
			ctrlPts.add(index, pt);
			calcCurves();
		}
		else {
			//throw new IndexOutOfBoundsException("Attempted to add control point at out-of-bounds index");
			System.err.println("Control point index out of bounds");
		}
	}

	/**
	 * Removes the last control point
	 * @return True if a point was removed
	 */
	public boolean removePt() {
		if (ctrlPts.size() > 0) {
			CtrlPt pt = ctrlPts.remove(ctrlPts.size() - 1);
			Main.sim.removeEntity(pt);
			calcCurves();
			return true;
		}

		return false;
	}

	/**
	 * Remove the specified point from the path
	 * @param ctrlPt Point to remove
	 */
    public void removePt(CtrlPt ctrlPt) {
        ctrlPts.remove(ctrlPt);
        Main.sim.removeEntity(ctrlPt);
        calcCurves();
    }

    /**
     * Get a copy of the list of control points
     * @return A clone (copy) of the internal control points list
     */
    @SuppressWarnings("unchecked")
    public List<CtrlPt> getCtrlPts() {
        return (List) ctrlPts.clone();
    }

	/**
	 * Reverses the path
	 */
	public void reverse() {
		ArrayList<CtrlPt> newPts = new ArrayList<CtrlPt>();

		for (int i = ctrlPts.size() - 1; i >= 0; i--) {
			newPts.add(ctrlPts.get(i));
		}

		ctrlPts = newPts;
		calcCurves();
	}

	/**
	 * Draw the full path
	 * @param g Graphics context to draw with
	 */
	public void draw(Graphics2D g) {
		// Draw lines
		for (StraightLine c : lines) {
			c.draw(g);
		}

		// Draw control points
		for (CtrlPt c : ctrlPts) {
		    c.draw(g);
		    if (c.selected) c.drawBounds(g);
		}
	}

    public String XMLTagName() {
        return "StraightPath";
    }
}
