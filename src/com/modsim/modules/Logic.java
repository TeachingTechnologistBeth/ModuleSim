package com.modsim.modules;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.modsim.modules.parts.*;
import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;
import com.modsim.res.Colors;
import com.modsim.util.BinData;

/**
 * Logic Unit
 *
 * @author aw12700
 *
 */
public class Logic extends BaseModule {

    private final LEDRow leds;
    private final List<LED> cLEDs;

    private final Output rOut;
    private final Output cOut;
    private final Input dInA;
    private final Input dInB;
    private final Input cIn;

    Logic() {
        w = 150;
        h = 150;

        // Add the inputs and outputs
        rOut = addOutput("Result", 0, Output.DATA);
        cOut = addOutput("Control out", 0, Output.CTRL);

        dInA = addInput("Input A", -25, Input.DATA);
        dInB = addInput("Input B", 25, Input.DATA);

        cIn = addInput("Control in", 0, Input.CTRL);

        leds = new LEDRow(35, -70);
        addPart(leds);

        // Label
        addPart(new SSText(-45, -15, "LU", 40, Colors.moduleLabel));

        // Function LEDs
        String[] labels = {"NOT", "AND", "OR", "XOR"};
        LED[] cLED = new LED[4];

        for (int i = 0; i < cLED.length; i++) {
            int xPos = (int) (50 * ((i%2) - 0.8));
            int yPos = 12 * ((i/2) + 2);

            cLED[i] = new LED(xPos, yPos);
            addPart(cLED[i]);

            addPart(new SSText(xPos + 10, yPos+4, labels[i], 12, Colors.moduleSubLabel));
        }

        cLEDs = Collections.unmodifiableList(Arrays.asList(cLED));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Logic();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);
        g.setColor(Colors.moduleInset);
        drawTrapezoid(g, 10, 0, 35, 120, 80);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        // Get the inputs
        int aVal = dInA.getVal().getUInt();
        int bVal = dInB.getVal().getUInt();

        int func = cIn.getVal().getUInt() & 3;
        int result = 0;

        for (int i = 0; i < cLEDs.size(); i++) {
            cLEDs.get(i).setEnabled(i == func);
        }

        switch (func) {
            case 0:
                result = ~aVal;
                break;
            case 1:
                result = aVal & bVal;
                break;
            case 2:
                result = aVal | bVal;
                break;
            case 3:
                result = aVal ^ bVal;
                break;
        }
        BinData r = new BinData();
        r.setUInt(result);

        // Set display
        leds.setVal(r);

        // Set outputs
        rOut.setVal(r);
        cOut.setVal(cIn.getVal());
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> outList = super.getAffected(in);
        if (in != cIn) {
            outList.remove(cOut);
        }

        return outList;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.LOGIC;
    }
}
