package com.modsim.modules.parts;

import com.modsim.modules.BaseModule;
import com.modsim.modules.Link;
import com.modsim.util.BinData;
import com.modsim.util.Vec2;

public abstract class Port {

	public enum Mode {
		MODE_INPUT,
		MODE_OUTPUT,
		MODE_BIDIR
	}

	public static Mode OppositeOf(Mode m) {
		switch (m) {
			case MODE_INPUT: return Mode.MODE_OUTPUT;
			case MODE_OUTPUT: return Mode.MODE_INPUT;

			case MODE_BIDIR: default: return Mode.MODE_BIDIR;
		}
	}

    public int ID;
	public BaseModule owner;

	public Link link;

	public static final int DATA = 0;
	public static final int CTRL = 1;
	public static final int CLOCK = 2;
	public static final int GENERIC = 3;

	public int type;
	public int side;
	public String text;
	public int pos = 0;

	protected BinData value = new BinData();

    public boolean updated = false;

    /**
     * Get the port's ID
     * @return The ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Value set. Registers output as updated
     * if value is changed.
     * @return True if the value was changed (if
     * it was different to the last stored value)
     */
    public boolean setVal(BinData val) {
        if (!val.equals(value)) {
            value = new BinData(val);
            updated = true;
            return true;
        }
        else {
            updated = false;
            return false;
        }
    }

    /**
     * Update check
     */
    public boolean wasUpdated() {
        return updated;
    }

	/**
	 * Value retrieval
	 */
	public BinData getVal() {
		return new BinData(value);
	}

	/**
	 * Retrieves the object-space position of the port
	 * @return Port's object-space position
	 */
	public Vec2 getDisplayPos() {
		Vec2 p = new Vec2();

		if (type == Port.CTRL || type == Port.CLOCK) {
			p.set(side*owner.w/2, -pos);
		}
		else {
			p.set(pos, side*owner.h/2);
		}

		return p;
	}

	/**
	 * Retrieves the world position of the port
	 * @return Port's world-space position
	 */
	public Vec2 getDisplayPosW() {
		return owner.objToWorld(getDisplayPos());
	}

	public abstract boolean canInput();
	public abstract boolean canOutput();
	public abstract boolean hasDirection();

	public abstract Mode getMode();
	public void setMode(Mode newMode) {}

}
