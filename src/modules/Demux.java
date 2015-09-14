package modules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import modules.parts.Input;
import modules.parts.LED;
import modules.parts.LEDRow;
import modules.parts.Label;
import modules.parts.Output;
import modules.parts.Port;
import util.BinData;

/**
 * Demultiplexor module
 * @author aw12700
 *
 */
public class Demux extends BaseModule {
    private final LEDRow dLEDs;
    private final List<LED> cLEDs;
    
    private final List<Output> dOut;
    private final Output contOut;
    private final Input dIn;
    private final Input contIn;

    Demux() {
        w = 150;
        h = 150;

        dIn = addInput("Input", 0, Port.DATA);
        contIn = addInput("Control in", 0, Port.CTRL);

        // Must wrap the array to make it immutable
        dOut = Collections.unmodifiableList(Arrays.asList(new Output[]{
            addOutput("Output A", -50, Port.DATA),
            addOutput("Output B", -25, Port.DATA),
            addOutput("Output C",  25, Port.DATA),
            addOutput("Output D",  50, Port.DATA)
        }));
        contOut = addOutput("Control out", 0, Port.CTRL);

        // Add display
        cLEDs = Collections.unmodifiableList(Arrays.asList(new LED[] {
            new LED(-50, -50),
            new LED(-25, -50),
            new LED( 25, -50),
            new LED( 50, -50),
        }));

        for (LED l : cLEDs) {
            addPart(l);
        }
        
        cLEDs.get(0).setEnabled(true);
        
        dLEDs = new LEDRow(35, 70);
        addPart(dLEDs);
        
        addPart(new Label(-45, 15, "DMX", 40, new Color(200,200,200)));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Demux();
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100,100,100));
        drawBox(g, 10);
        g.setColor(new Color(80,80,80));
        drawTrapezoid(g, 10, 0, -55, 130, -40);

        // Show IO
        g.setColor(new Color(120,120,120));
        drawInputs(g);
        drawOutputs(g);

        // Show LEDs
        drawParts(g);
    }

    @Override
    public void propagate() {
        final int outSel = contIn.getVal().getUInt() & 3;

        for (int i = 0; i < dOut.size(); i++) {
            if (i == outSel) {
                dOut.get(i).setVal(dIn.getVal());
                cLEDs.get(i).setEnabled(true);
            } else {
                dOut.get(i).setVal(new BinData());
                cLEDs.get(i).setEnabled(false);
            }
        }

        contOut.setVal(contIn.getVal());

        dLEDs.setVal(dIn.getVal());
    }

    @Override
    protected void reset() {
        // Noop
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.DEMUX;
    }

}
