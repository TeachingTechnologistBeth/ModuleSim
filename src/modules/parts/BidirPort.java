package modules.parts;

import util.BinData;

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
                if (fixedConnection(this)) return;
            }

            mode = newMode;
            owner.propagateDirectionality(this);
        }
    }

    private boolean fixedConnection(BidirPort root) {
        boolean foundFixed = false;

        for (BidirPort bp : root.owner.bidirs) {
            if (bp == root) continue;

            if (bp.link != null) {
                if (bp.link.targ.getClass() != BidirPort.class || bp.link.src.getClass() != BidirPort.class) {
                    foundFixed = true;
                }
                else if (bp.link.src == bp) {
                    foundFixed |= fixedConnection((BidirPort) bp.link.targ);
                }
                else if (bp.link.targ == bp) {
                    foundFixed |= fixedConnection((BidirPort) bp.link.src);
                }
            }
        }

        return foundFixed;
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

}
