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
import com.modsim.util.BinData;

/**
 * Demultiplexor module
 * @author aw12700
 *
 */
public class Demux extends BaseModule {
    private final LEDRow dataLEDs;
    private final List<LED> controlLEDs;

    private final List<Output> dataOutputs;
    private final Output controlOut;
    private final Input dataIn;
    private final Input controlIn;

    Demux() {
        w = 150;
        h = 150;

        dataIn = addInput("Input", 0, Port.DATA);
        controlIn = addInput("Control in", 0, Port.CTRL);

        // Must wrap the array to make it immutable
        dataOutputs = Collections.unmodifiableList(Arrays.asList(
            addOutput("Output A", -50, Port.DATA),
            addOutput("Output B", -25, Port.DATA),
            addOutput("Output C",  25, Port.DATA),
            addOutput("Output D",  50, Port.DATA)
        ));
        controlOut = addOutput("Control out", 0, Port.CTRL);

        // Add display
        controlLEDs = Collections.unmodifiableList(Arrays.asList(
            new LED(-50, -50),
            new LED(-25, -50),
            new LED( 25, -50),
            new LED( 50, -50)
        ));

        for (LED l : controlLEDs) {
            addPart(l);
        }

        controlLEDs.get(0).setEnabled(true);

        dataLEDs = new LEDRow(35, 70);
        addPart(dataLEDs);

        addPart(new SSText(-45, 15, "DMX", 40, Colors.moduleLabel));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Demux();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);
        g.setColor(Colors.moduleInset);
        drawTrapezoid(g, 10, 0, -55, 130, -40);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        final int outSel = controlIn.getVal().getUInt() & 3;

        for (int i = 0; i < dataOutputs.size(); i++) {
            if (i == outSel) {
                dataOutputs.get(i).setVal(dataIn.getVal());
                controlLEDs.get(i).setEnabled(true);
            } else {
                dataOutputs.get(i).setVal(new BinData(0));
                controlLEDs.get(i).setEnabled(false);
            }
        }

        controlOut.setVal(controlIn.getVal());

        dataLEDs.setVal(dataIn.getVal());
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> outList = super.getAffected(in);
        if (in != controlIn) {
            outList.remove(controlOut);
        }

        return outList;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.DEMUX;
    }

}
