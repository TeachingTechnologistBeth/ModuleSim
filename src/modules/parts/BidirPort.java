package modules.parts;

import util.BinData;

import java.util.ArrayDeque;
import java.util.ArrayList;

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
                if (!fixedConnection(this, new ArrayDeque<>(), new ArrayList<>())) {
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

    private boolean fixedConnection(
            BidirPort root,
            ArrayDeque<BidirPort> checkList,
            ArrayList<BidirPort> checked) {
        boolean foundFixed = false;
        checked.add(root);

        for (BidirPort bp : root.owner.bidirs) {
            if (bp.link != null && !checkList.contains(bp) && !checked.contains(bp)) {
                checked.add(bp);

                if (bp.link.targ.getClass() != BidirPort.class || bp.link.src.getClass() != BidirPort.class) {
                    foundFixed = true;
                }
                else if (bp.link.src == bp) {
                    checkList.add((BidirPort) bp.link.targ);
                }
                else if (bp.link.targ == bp) {
                    checkList.add(((BidirPort) bp.link.src));
                }
            }
        }

        while (checkList.peekFirst() != null) {
            BidirPort bp = checkList.removeFirst();
            foundFixed |= fixedConnection(bp, checkList, checked);
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
