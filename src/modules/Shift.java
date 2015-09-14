package modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import modules.parts.Input;
import modules.parts.LED;
import modules.parts.LEDRow;
import modules.parts.Output;
import modules.parts.Port;
import util.BinData;

public class Shift extends BaseModule {
    
    // Pull control to XX01
    private static final BinData contPull = new BinData((byte)1, (byte)3);
    
    private final boolean left;
    
    private final Output dOut;
    private final Output chOut;
    private final Output contOut;
    private final Input contIn;
    private final Input dIn;
    private final Input chIn;
    
    private final LEDRow dLEDs;
    private final LED[] cLEDs = new LED[2];
    
    Shift(boolean left) {
        w = 150;
        h = 50;
        this.left = left;

        dOut = addOutput("Result", 0, Port.DATA);
        chOut = addOutput("Chain out", left ? -50 : 50, Port.DATA);
        contOut = addOutput("Control out", 0, Port.CTRL);

        contIn = addInput("Control in", 0, Port.CTRL, contPull);
        dIn = addInput("Data in", 0, Port.DATA);
        chIn = addInput("Chain in", left ? 50 : -50, Port.DATA);
        
        dLEDs = new LEDRow(left ? 35 : -35, -20);
        addPart(dLEDs);

        // Add display
        cLEDs[0] = new LED(50, 10);
        cLEDs[1] = new LED(50, 0);
        
        // Set initial LED state according to pull.
        for (int i = 0; i < cLEDs.length; i++) {
            cLEDs[i].setEnabled(contIn.getVal().getBit(i) == BinData.HIGH);
            addPart(cLEDs[i]);
        }
        
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Shift(left);
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100, 100, 100));
        drawTrapezoid(g, 10);

        // Show output/input
        g.setColor(new Color(120, 120, 120));
        drawOutputs(g);
        drawInputs(g);
        
        // Show LEDS
        drawParts(g);

        // Show label
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString(left ? "LSH" : "RSH", -20, 8);
    }

    @Override
    public void propagate() {
        // Get the input values
        byte[] chainOut = new byte[4];
        chainOut[3] = BinData.NOCON;
        
        switch (contIn.getVal().getBit(0)) {
            case 0:
                // xxx0
                chainOut[0] = dIn.getVal().getBit(iSided(0));
                chainOut[1] = dIn.getVal().getBit(iSided(0));
                chainOut[2] = dIn.getVal().getBit(iSided(1));
                switch (contIn.getVal().getBit(1)) {
                    case 1:
                        // xx10
                        dOut.setVal(shift(dIn.getVal(), 2, chIn.getVal()));
                        break;
                    default:
                        // xx00
                        dOut.setVal(dIn.getVal());
                        break;
                }
                break;
            default:
                // xxx1
                chainOut[0] = dIn.getVal().getBit(iSided(0));
                chainOut[1] = dIn.getVal().getBit(iSided(1));
                chainOut[2] = dIn.getVal().getBit(iSided(2));
                switch (contIn.getVal().getBit(1)) {
                    case 1:
                        // xx11
                        dOut.setVal(shift(dIn.getVal(), 3, chIn.getVal()));
                        break;
                    default:
                        // xx01
                        dOut.setVal(shift(dIn.getVal(), 1, chIn.getVal()));
                        break;
                }
        }
        
        cLEDs[0].setEnabled(contIn.getVal().getBit(0) == BinData.HIGH);
        cLEDs[1].setEnabled(contIn.getVal().getBit(1) == BinData.HIGH);
        dLEDs.setVal(dOut.getVal());
        
        chOut.setVal(new BinData(chainOut));
        contOut.setVal(contIn.getVal());
    }
    
    private int iSided(int which) {
        if (left) {
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
        
        if (left) {
            System.arraycopy(ret, 0, ret, dist, ret.length - dist);
        } else {
            System.arraycopy(ret, dist, ret, 0, ret.length - dist);
        }
        
        // Set the missing bits according to chain
        switch (dist) {
            case 1:
                ret[(left ? 0 : 3)] = chain.getBit(0);
                break;
            case 3:
                ret[(left ? 2 : 1)] = chain.getBit(0);
                // Don't break here
            case 2:
                if (left) {
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
    protected void reset() {
        // Noop
    }

    @Override
    public AvailableModules getModType() {
        if (left) {
            return AvailableModules.LEFT_SHIFT;
        }
        else {
            return AvailableModules.RIGHT_SHIFT;
        }
    }
}
