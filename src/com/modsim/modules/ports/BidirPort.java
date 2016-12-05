package com.modsim.modules.ports;

import com.modsim.modules.BaseModule;
import com.modsim.modules.parts.Port;
import com.modsim.util.BinData;

/**
 * Created by awick on 15/09/2015.
 */
public class BidirPort extends Port {

    protected Mode mode = Mode.MODE_BIDIR;

    public BidirPort(int visible_side) {
        side = visible_side;
    }

    /**
     * Retrieve the bidirectional port's operating mode
     * @return Operating mode (MODE_INPUT, MODE_OUTPUT, or MODE_BIDIR)
     */
    @Override
    public Mode getMode() { return mode; }

    /**
     * Set the bidirectional port's operating mode
     * @param newMode Mode to operate in
     */
    @Override
    public void setMode(Mode newMode) {
        if (mode != newMode) {
            if (newMode == Mode.MODE_BIDIR) {
                // Need to make sure there are no fixed connections
                if (!hasLinks(owner)) {
                    mode = newMode;
                    owner.propagateDirectionality(this);
                }
            }
            else {
                mode = newMode;
                if (link != null) owner.propagateDirectionality(this);
            }

        }
    }

    private boolean hasLinks(BaseModule m) {
        for (Port p : m.ports) {
            if (p.link != null) return true;
        }

        return false;
    }

    @Override
    public boolean setVal(BinData val) {
        boolean retVal = false;
        switch (mode) {
            case MODE_INPUT:
            case MODE_OUTPUT:
                retVal = super.setVal(val);
                break;
        }

        return retVal;
    }

    @Override
    public BinData getVal() {
        BinData retVal = new BinData();
        switch (mode) {
            case MODE_INPUT:
            case MODE_OUTPUT:
                retVal.set(super.getVal());
                break;
        }

        return retVal;
    }

    @Override
    public boolean canInput() {
        switch (mode) {
            case MODE_BIDIR:
            case MODE_INPUT:
                return true;
            case MODE_OUTPUT:
            default:
                return false;
        }
    }

    @Override
    public boolean canOutput() {
        switch (mode) {
            case MODE_BIDIR:
            case MODE_OUTPUT:
                return true;
            case MODE_INPUT:
            default:
                return false;
        }
    }

    @Override
    public boolean hasDirection() {
        return !(mode == Mode.MODE_BIDIR);
    }

	public boolean isConnected() {
		BinData data = this.getVal();
		for(int i = 0; i<4; i++)
		{
			if(data.getBit(i)!=BinData.NOCON) return true;
		}
		return false;
	}

}
