package com.modsim.modules;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.modsim.modules.parts.SSText;
import com.modsim.modules.ports.Input;
import com.modsim.modules.parts.LED;
import com.modsim.modules.parts.LEDRow;
import com.modsim.modules.ports.Output;
import com.modsim.modules.parts.Port;
import com.modsim.res.Colors;

/**
 * Multiplexor module
 * @author aw12700
 *
 */
public class Mux extends BaseModule {
    private final LEDRow dLEDs;
    private final List<LED> cLEDs;

    private final Output dOut;
    private final Output contOut;
    private final List<Input> dIn;
    private final Input contIn;

    Mux() {
        w = 150;
        h = 150;

        dOut = addOutput("Selected output", 0, Port.DATA);
        contOut = addOutput("Control out", 0, Port.CTRL);

        dIn = Collections.unmodifiableList(Arrays.asList(new Input[]{
            addInput("Input A", -50, Port.DATA),
            addInput("Input B", -25, Port.DATA),
            addInput("Input C",  25, Port.DATA),
            addInput("Input D",  50, Port.DATA),
        }));
        contIn = addInput("Control in", 0, Port.CTRL);

        // Add display
        cLEDs = Collections.unmodifiableList(Arrays.asList(new LED[] {
            new LED(-50, 50),
            new LED(-25, 50),
            new LED( 25, 50),
            new LED( 50, 50),
        }));

        for (LED l : cLEDs) {
            addPart(l);
        }

        dLEDs = new LEDRow(35, -70);
        addPart(dLEDs);

        addPart(new SSText(-45, 15, "MUX", 40, Colors.moduleLabel));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Mux();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);
        g.setColor(Colors.moduleInset);
        drawTrapezoid(g, 10, 0, 55, 130, 40);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        final int sel = contIn.getVal().getUInt() & 3;

        dOut.setVal(dIn.get(sel).getVal());
        dLEDs.setVal(dIn.get(sel).getVal());

        for (int i = 0; i < 4; i++) {
            cLEDs.get(i).setEnabled(i == sel);
        }

        contOut.setVal(contIn.getVal());
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> outList = super.getAffected(in);
        if (in != contIn) {
            outList.remove(contOut);
        }

        return outList;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.MUX;
    }

}
