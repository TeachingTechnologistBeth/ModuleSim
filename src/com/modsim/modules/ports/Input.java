package com.modsim.modules.ports;

import com.modsim.modules.parts.Port;
import com.modsim.util.BinData;

/**
 * Input port for a module
 * @author aw12700
 *
 */
public class Input extends Port {

    public BinData pull = new BinData(0);

    public Input() {
        side = 1;
    }

    @Override
    public BinData getVal() {
        if (link != null) {
            BinData v = super.getVal();
            v.resolvePull(pull);
            return v;
        }
        else {
            return pull;
        }
    }

    @Override
    public boolean canInput() {
        return true;
    }

    @Override
    public boolean canOutput() {
        return false;
    }

    @Override
    public boolean hasDirection() {
        return true;
    }

    @Override
    public Mode getMode() {
        return Mode.MODE_INPUT;
    }
}
