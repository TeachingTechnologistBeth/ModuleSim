package com.modsim.modules;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.modsim.modules.ports.Input;
import com.modsim.modules.parts.LED;
import com.modsim.modules.parts.LEDRow;
import com.modsim.modules.ports.Output;
import com.modsim.modules.parts.Port;
import com.modsim.res.Colors;
import com.modsim.util.BinData;

public class Shift extends BaseModule {

    // Pull control to XX01
    private static final BinData contPull = new BinData((byte)1, (byte)3);

    private final boolean isLeftShift;

    private final Output dataOut;
    private final Output chainOut;
    private final Output controlOut;
    private final Input controlIn;
    private final Input dataIn;
    private final Input chainIn;

    private final LEDRow dataLEDs;
    private final LED[] shiftLEDs = new LED[2];

    Shift(boolean left) {
        w = 150;
        h = 50;
        isLeftShift = left;

        dataOut = addOutput("Result", 0, Port.DATA);
        chainOut = addOutput("Chain out", left ? -50 : 50, Port.DATA);
        controlOut = addOutput("Control out", 0, Port.CTRL);

        controlIn = addInput("Control in", 0, Port.CTRL, contPull);
        dataIn = addInput("Data in", 0, Port.DATA);
        chainIn = addInput("Chain in", left ? 50 : -50, Port.DATA);

        dataLEDs = new LEDRow(left ? 35 : -35, -20);
        addPart(dataLEDs);

        // Add display
        shiftLEDs[0] = new LED(50, 10);
        shiftLEDs[1] = new LED(50, 0);

        // Set initial LED state according to pull.
        for (int i = 0; i < shiftLEDs.length; i++) {
            shiftLEDs[i].setEnabled(controlIn.getVal().getBit(i) == BinData.HIGH);
            addPart(shiftLEDs[i]);
        }

        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Shift(isLeftShift);
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawTrapezoid(g, 10);

        // Show output/input
        g.setColor(Colors.modulePorts);
        drawOutputs(g);
        drawInputs(g);

        // Show label
        g.setColor(Colors.moduleLabel);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString(isLeftShift ? "LSH" : "RSH", -20, 8);

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        // Get the input values
        byte[] chainOut = new byte[4];
        chainOut[3] = BinData.NOCON;

        switch (controlIn.getVal().getBit(0)) {
            case 0:
                // xxx0
                chainOut[0] = dataIn.getVal().getBit(iSided(0));
                chainOut[1] = dataIn.getVal().getBit(iSided(0));
                chainOut[2] = dataIn.getVal().getBit(iSided(1));
                switch (controlIn.getVal().getBit(1)) {
                    case 1:
                        // xx10
                        dataOut.setVal(shift(dataIn.getVal(), 2, chainIn.getVal()));
                        break;
                    default:
                        // xx00
                        dataOut.setVal(dataIn.getVal());
                        break;
                }
                break;
            default:
                // xxx1
                chainOut[0] = dataIn.getVal().getBit(iSided(0));
                chainOut[1] = dataIn.getVal().getBit(iSided(1));
                chainOut[2] = dataIn.getVal().getBit(iSided(2));
                switch (controlIn.getVal().getBit(1)) {
                    case 1:
                        // xx11
                        dataOut.setVal(shift(dataIn.getVal(), 3, chainIn.getVal()));
                        break;
                    default:
                        // xx01
                        dataOut.setVal(shift(dataIn.getVal(), 1, chainIn.getVal()));
                        break;
                }
        }

        shiftLEDs[0].setEnabled(controlIn.getVal().getBit(0) == BinData.HIGH);
        shiftLEDs[1].setEnabled(controlIn.getVal().getBit(1) == BinData.HIGH);
        dataLEDs.setVal(dataOut.getVal());

        this.chainOut.setVal(new BinData(chainOut));
        controlOut.setVal(controlIn.getVal());
    }

    private int iSided(int which) {
        if (isLeftShift) {
            switch (which) {
                case 0:
                    return 3;
                case 1:
                    return 2;
                case 2:
                    return 1;
                default:
                    return 0;
            }
        } else {
            return which;
        }
    }

    private BinData shift(BinData in, int dist, BinData chain) {
        byte[] ret = in.getAll();

        if (isLeftShift) {
            System.arraycopy(ret, 0, ret, dist, ret.length - dist);
        } else {
            System.arraycopy(ret, dist, ret, 0, ret.length - dist);
        }

        // Set the missing bits according to chain
        switch (dist) {
            case 1:
                ret[(isLeftShift ? 0 : 3)] = chain.getBit(0);
                break;
            case 3:
                ret[(isLeftShift ? 2 : 1)] = chain.getBit(0);
                // Don't break here
            case 2:
                if (isLeftShift) {
                    ret[0] = chain.getBit(2);
                    ret[1] = chain.getBit(1);
                } else {
                    ret[3] = chain.getBit(2);
                    ret[2] = chain.getBit(1);
                }
                break;
        }

        return new BinData(ret);
    }

    @Override
    public List<Port> getAffected(Port in) {
        // Control-out is only affected by control-in, other than that everything's variable
        List<Port> outPorts = super.getAffected(in);
        if (in != controlIn) {
            outPorts.remove(controlOut);
        }

        return outPorts;
    }

    @Override
    public AvailableModules getModType() {
        if (isLeftShift) {
            return AvailableModules.LEFT_SHIFT;
        }
        else {
            return AvailableModules.RIGHT_SHIFT;
        }
    }
}
