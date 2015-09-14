package modules.parts;

import modules.BaseModule;
import modules.Link;
import util.BinData;
import util.Vec2;

public class Port {
	
    public int ID;
	public BaseModule owner;
	
	public Link link;
	
	public static final int DATA = 0;
	public static final int CTRL = 1;
	public static final int CLK = 2;
	public static final int GENERIC = 3;
	
	public int type;
	public int side;
	public String text;
	public int pos = 0;
	public boolean bidir = false;
	public boolean isOutput = false;
	
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
	 * @return
	 */
	public Vec2 getDisplayPos() {
		Vec2 p = new Vec2();
		
		int mul = 0;
		if (getClass().equals(Input.class)) mul = 1;
		else mul = -1;
		
		if (type == Port.CTRL || type == Port.CLK) {
			p.set(mul*owner.w/2, -pos);
		}
		else {
			p.set(pos, mul*owner.h/2);
		}
		
		return p;
	}
	
	/**
	 * Retrieves the world position of the port
	 * @return
	 */
	public Vec2 getDisplayPosW() {
		return owner.objToWorld(getDisplayPos());
	}
	
}
