package com.modsim.modules.ports;


import com.modsim.modules.parts.Port;

/**
 * Output port for a module
 * Binds an output value during propagation
 * @author aw12700
 *
 */
public class Output extends Port {

    public Output() {
        side = -1;
    }

    @Override
    public boolean canInput() {
        return false;
    }

    @Override
    public boolean canOutput() {
        return true;
    }

    @Override
    public boolean hasDirection() {
        return true;
    }

    @Override
    public Mode getMode() {
        return Mode.MODE_OUTPUT;
    }
}
