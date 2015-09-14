package modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import modules.parts.Input;
import modules.parts.Output;
import modules.parts.Port;
import simulator.PickableEntity;
import util.BezierCurve;
import util.BinData;
import util.Vec2;

public class SplitMerge extends BaseModule {

    private final Input inA;
    private final Input inB;
    
    private final Output outA;
    private final Output outB;
    private final Output outC;
    private final Output outD;
    
    private final List<BezierCurve> curves;
    
    public SplitMerge() {
        w = 150;
        h = 50;
        
        // Add bi-directional inputs/outputs
        inA = addInput("4-Bit Port A", -25, Port.GENERIC);
        inB = addInput("2-Bit Port B",  25, Port.GENERIC);
        inA.bidir = true; inB.bidir = true;
        
        outA = addOutput("Port a", -50, Port.GENERIC);
        outB = addOutput("Port b", -25, Port.GENERIC);
        outC = addOutput("Port c",  25, Port.GENERIC);
        outD = addOutput("Port d",  50, Port.GENERIC);
        outA.bidir = true; outB.bidir = true;
        outC.bidir = true; outD.bidir = true;
        
        // Curves - basically a visual wiring guide
        BezierCurve[] cs = new BezierCurve[9];
        
        // offsets
        int A, B, a, b, c, d, b0, b1, b2, b3;
        A = -25; B = 25;
        a = -50; b = -25; c = 25; d = 50;
        b0 = 6; b1 = 2; b2 = -2; b3 = -6;
        
        // curves to draw
        cs[0] = new BezierCurve(new Vec2(A + b0,  23),  new Vec2(a - b0, -23), // A0-a0
                                new Vec2(A + b0, -17),  new Vec2(a - b0, 13));
        
        cs[1] = new BezierCurve(new Vec2(A + b1,  23),  new Vec2(b - b0, -23), // A1-b0
                                new Vec2(A + b1, -15),  new Vec2(b - b0, 15));
        
        cs[2] = new BezierCurve(new Vec2(A + b1,  23),  new Vec2(a - b1, -23), // A1-a1
                                new Vec2(A + b1, -13),  new Vec2(a - b1, 17));
        
        cs[3] = new BezierCurve(new Vec2(A + b2,  23),  new Vec2(c - b0, -23), // A2-c0
                                new Vec2(A + b2, -15),  new Vec2(c - b0, 15));
        
        cs[4] = new BezierCurve(new Vec2(A + b3,  23),  new Vec2(d - b0, -23), // A3-d0
                                new Vec2(A + b3, -20),  new Vec2(d - b0, 5));
        
        cs[5] = new BezierCurve(new Vec2(A + b3,  23),  new Vec2(c - b1, -23), // A3-c1
                                new Vec2(A + b3, -17),  new Vec2(c - b1, 8));
        
        
        cs[6] = new BezierCurve(new Vec2(B + b0,  23),  new Vec2(c - b0, -23), // B0-c0
                                new Vec2(B + b0, -15),  new Vec2(c - b0, 15));
        
        cs[7] = new BezierCurve(new Vec2(B + b1,  23),  new Vec2(c - b1, -23), // B1-c1
                                new Vec2(B + b1, -15),  new Vec2(c - b1, 15));
        
        cs[8] = new BezierCurve(new Vec2(B + b1,  23),  new Vec2(d - b0, -23), // B1-d0
                                new Vec2(B + b1, -15),  new Vec2(d - b0, 15));
        curves = Collections.unmodifiableList(Arrays.asList(cs));
    }
    
    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100, 100, 100));
        drawBox(g, 10);

        // Show drawing
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < curves.size(); i++) {
            int grad = 0;
            switch (i) {
            case 0:
                grad = 120;
                break;
            case 1:
            case 2:
                grad = 140;
                break;
            case 3:
                grad = 160;
                break;
            case 4:
            case 5:
                grad = 180;
                break;
            case 6:
                grad = 200;
                break;
            case 7:
            case 8:
                grad = 220;
                break;
            }
            g.setColor(new Color(grad, grad, grad));
            curves.get(i).draw(g);
        }
        
        // Show output/input
        g.setColor(new Color(120, 120, 120));
        drawOutputs(g);
        drawInputs(g);
        
        // Show LEDs
        drawParts(g);
    }
    
    @Override
    public void propagate() {
        // Connections
        BinData AVal, BVal, aVal, bVal, cVal, dVal;
        AVal = inA.getVal();
        BVal = inB.getVal();
        aVal = outA.getVal();
        bVal = outB.getVal();
        cVal = outC.getVal();
        dVal = outD.getVal();
        
        // Resolve direction of propagation
        if (inA.wasUpdated() || inB.wasUpdated()) {
            inA.isOutput = false;
            inB.isOutput = false;
            outA.isOutput = true;
            outB.isOutput = true;
            outC.isOutput = true;
            outD.isOutput = true;
            
            aVal.setBit(0, AVal.getBit(0)); // A0-a0
            bVal.setBit(0, AVal.getBit(1)); // A1-b1
            aVal.setBit(1, AVal.getBit(1)); // A1-a1
            
            // Resolution of 3-state logic for merges
            dVal.setBit(0, AVal.getBit(3));     // A3-d0
            dVal.resolveBit(0, BVal.getBit(1)); // B1-d0
            
            cVal.setBit(0, AVal.getBit(2));     // A2-c0
            cVal.resolveBit(0, BVal.getBit(0)); // B0-c0
            
            cVal.setBit(1, AVal.getBit(3));     // A3-c1
            cVal.resolveBit(1, BVal.getBit(1)); // B1-c1
        }
        else if (   outA.wasUpdated() || outB.wasUpdated() ||
                    outC.wasUpdated() || outD.wasUpdated()) {
            outA.isOutput = false;
            outB.isOutput = false;
            outC.isOutput = false;
            outD.isOutput = false;
            inA.isOutput = true;
            inB.isOutput = true;
            
            AVal.setBit(0, aVal.getBit(0)); // a0-A0
            AVal.setBit(2, cVal.getBit(0)); // c0-A2
            AVal.setBit(3, cVal.getBit(1)); // c1-A3
            BVal.setBit(0, cVal.getBit(0)); // c0-B0
            
            // Resolution of 3-state logic for merges
            BVal.setBit(1, cVal.getBit(1));     // c1-B1
            BVal.resolveBit(1, dVal.getBit(0)); // d0-B1
            
            AVal.setBit(1, aVal.getBit(1));     // a1-A1
            AVal.resolveBit(1, bVal.getBit(0)); // b0-A1
        }
        
        // Set the values
        inA.setVal(AVal);
        inB.setVal(BVal);
        
        outA.setVal(aVal);
        outB.setVal(bVal);
        outC.setVal(cVal);
        outD.setVal(dVal);
    }

    @Override
    protected void reset() {
        // Noop
    }

    @Override
    public PickableEntity createNew() {
        return new SplitMerge();
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.SPLIT_MERGE;
    }
    
}
