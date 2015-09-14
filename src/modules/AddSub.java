package modules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import util.BinData;
import modules.parts.Input;
import modules.parts.LED;
import modules.parts.LEDColour;
import modules.parts.LEDRow;
import modules.parts.Label;
import modules.parts.Output;

/**
 * Arithmetic Unit
 *
 * @author aw12700
 *
 */
public class AddSub extends BaseModule {
    
    public static final AvailableModules source = AvailableModules.ADDSUB;

    private final LEDRow leds;
    private final LED carryLED;
    private final LED boolLED;
    
    private final List<LED> cLEDs;
    
    private final Output rOut;
    private final Output bOut;
    private final Output cOut;
    private final Input dInA;
    private final Input dInB;
    private final Input cIn;

    AddSub() {
        w = 150;
        h = 150;

        // Add the inputs and outputs
        rOut = addOutput("Result", 0, Output.DATA);
        bOut = addOutput("Bool test Result", -25, Output.DATA);

        cOut = addOutput("Control out", 0, Output.CTRL);

        dInA = addInput("Input A", -25, Input.DATA);
        dInB = addInput("Input B", 25, Input.DATA);

        cIn = addInput("Control in", 0, Input.CTRL);

        // Label
        addPart(new Label(-45, -15, "AU", 40, new Color(200, 200, 200)));
        
        // Add display
        carryLED = new LED(-52, 6);
        addPart(carryLED);
        addPart(new Label(-45, 9, "CARRY OUT", 10));
        leds = new LEDRow(35, -70);
        addPart(leds);
        boolLED = new LED(-45, -70, LEDColour.RED);
        addPart(boolLED);
        
        // Function LEDs
        String[] labels = {"ADD | Z?", "PSS | Z?", "SUB | Z?", "SUB | N?"};
        LED[] cLED = new LED[4];
        
        for (int i = 0; i < cLED.length; i++) {
            int xPos = (int) (65 * ((i%2) - 0.8));
            int yPos = 12 * ((i/2) + 3);
            
            cLED[i] = new LED(xPos, yPos);
            addPart(cLED[i]);
            
            addPart(new Label(xPos + 7, yPos+4, labels[i], 10));
        }
        
        cLEDs = Collections.unmodifiableList(Arrays.asList(cLED));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new AddSub();
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100, 100, 100));
        drawBox(g, 10);
        g.setColor(new Color(80,80,80));
        drawTrapezoid(g, 10, 0, 35, 130, 80);

        // Show IO
        g.setColor(new Color(120, 120, 120));
        drawInputs(g);
        drawOutputs(g);

        // Show LEDs
        drawParts(g);
    }

    @Override
    public void propagate() {
        // Inputs as ints
        int a = dInA.getVal().getUInt();
        int b = dInB.getVal().getUInt();
        BinData cInV = cIn.getVal();

        int func = cInV.getUInt() & 3;
        for (int i = 0; i < cLEDs.size(); i++) {
            cLEDs.get(i).setEnabled(i == func);
        }
        
        // Carry bit
        int carry = cInV.getBit(2);

        // Negation on B
        int comp = cInV.getBit(1) == 1 ? 0xF : 0;
        b = b ^ comp;

        // Calculation
        int result;
        if (cInV.getBit(0) == 1 && cInV.getBit(1) == 0) {
            result = a; // pass-through behaviour
        } else {
            result = a + b + carry;
        }

        // Get the carry bit
        int carryOut = (result >> 4) & 1;
        result = result & 0xF;

        // Result data
        BinData r = new BinData();
        r.setUInt(result);

        // Control out
        BinData cOutVal = new BinData(cInV);
        cOutVal.setBit(2, carryOut);
        boolean not0 = cInV.getBooleanBit(3) || result != 0;
        cOutVal.setBooleanBit(3, not0);

        // Boolean out
        boolean bool;
        if (cIn.getVal().getBit(0) == 1 && cIn.getVal().getBit(1) == 1) {
            // Negative test
            bool = r.getBit(3) == BinData.HIGH;
        } else {
            // Zero test
            bool = result == 0 && !not0;
        }
        BinData boolOut = new BinData();
        boolOut.setBool(bool);
        boolLED.setEnabled(bool);

        // Display
        carryLED.setEnabled(carryOut == 1);
        leds.setVal(r);

        // Outputs
        rOut.setVal(r);
        bOut.setVal(boolOut);
        cOut.setVal(cOutVal);
    }

    @Override
    protected void reset() {
        // Noop
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.ADDSUB;
    }
}
