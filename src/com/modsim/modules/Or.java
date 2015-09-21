package com.modsim.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.modsim.modules.parts.Input;
import com.modsim.modules.parts.LED;
import com.modsim.res.Colors;
import com.modsim.res.Colors.LEDColour;
import com.modsim.modules.parts.Output;
import com.modsim.modules.parts.Port;
import com.modsim.res.Fonts;
import com.modsim.util.BinData;

public class Or extends BaseModule {

    private final LED rLED;

    private final List<Output> passOut;
    private final Output rOut;
    private final List<Input> dIn;
    private final Input chIn;

    Or() {
        w = 150;
        h = 50;

        // Output
        rOut = addOutput("Or'ed output", 0, Port.CTRL);
        passOut = Collections.unmodifiableList(Arrays.asList(
                addOutput("Pass A", -50, Port.GENERIC),
                addOutput("Pass B", -25, Port.GENERIC),
                addOutput("Pass C", 25, Port.GENERIC),
                addOutput("Pass D", 50, Port.GENERIC)
        ));

        // Inputs
        chIn = addInput("Chain in", 0, Port.CTRL);
        dIn = Collections.unmodifiableList(Arrays.asList(
                addInput("Input A", -50, Port.GENERIC),
                addInput("Input B", -25, Port.GENERIC),
                addInput("Input C", 25, Port.GENERIC),
                addInput("Input D", 50, Port.GENERIC)
        ));

        rLED = new LED(-60, 0, LEDColour.RED);
        addPart(rLED);
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Or();
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawTrapezoid(g, 10);

        // Show output/input
        g.setColor(Colors.modulePorts);
        drawOutputs(g);
        drawInputs(g);

        // Show label
        g.setColor(Colors.moduleLabel);
        g.setFont(Fonts.moduleLabel);
        g.drawString("OR", -16, 8);

        // Show LED
        drawParts(g);
    }

    @Override
    public void propagate() {
        BinData res = new BinData();
        res.setBit(0, BinData.LOW);
        rLED.setEnabled(false);

        for (int i = 0; i < passOut.size(); i++) {
            passOut.get(i).setVal(dIn.get(i).getVal());

            byte v = passOut.get(i).getVal().getBit(0);
            if (v == BinData.HIGH) {
                res.setBit(0, BinData.HIGH);
                rLED.setEnabled(true);
            }
        }

        if (chIn.getVal().getBit(0) == BinData.HIGH) {
            res.setBit(0, BinData.HIGH);
            rLED.setEnabled(true);
        }

        rOut.setVal(res);
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.OR;
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> ret = new ArrayList<>();

        // Result is affected by everything
        ret.add(rOut);

        // Pass-through(s) only affected by their own inputs
        int ind = dIn.indexOf(in);
        if (ind != -1) {
            ret.add(passOut.get(dIn.indexOf(in)));
        }

        return ret;
    }
}
