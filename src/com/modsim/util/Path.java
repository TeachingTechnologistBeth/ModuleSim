package com.modsim.util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.modsim.modules.parts.Port;
import com.modsim.Main;

public abstract class Path {

    public class PointInfo {
        public Vec2 pt;
        public double dist;
        public double t;
        public int curveIndex;

        public String toString() {
            return pt.toString() + ", " + dist + ", " + t + ", " + curveIndex;
        }
    }

	protected ArrayList<CtrlPt> ctrlPts = new ArrayList<>();

    public abstract PointInfo approxClosestPoint(Vec2 searchPt, int iterations);
    public abstract CtrlPt closestCtrlPt(Vec2 searchPt);

    public abstract Path duplicate();

    public abstract void updateContours();

    public abstract void calcCurves();

    public abstract void draw(Graphics2D g);

    public abstract List<CtrlPt> getCtrlPts();

    public abstract void setStart(Vec2 end);
	public abstract void setStart(Vec2 end, Vec2 c);
	public abstract void setStart(Port p);
    public abstract void setEnd(Vec2 end);
	public abstract void setEnd(Vec2 end, Vec2 c);
	public abstract void setEnd(Port p);

    public abstract void reverse();

    public abstract void addPt(CtrlPt pt);
    public abstract void addPt(int index, CtrlPt pt);
    public abstract boolean removePt();
    public abstract void removePt(CtrlPt ctrlPt);

    public abstract String XMLTagName();
}
