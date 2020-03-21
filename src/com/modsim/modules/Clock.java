package com.modsim.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;

import com.modsim.res.Colors;
import com.modsim.res.Fonts;
import com.modsim.util.BinData;
import com.modsim.modules.parts.LED;
import com.modsim.res.Colors.LEDColour;
import com.modsim.modules.parts.Port;
import com.modsim.modules.parts.PushBtn;

/**
 * Clock module
 * @author aw12700
 *
 */
public class Clock extends BaseModule {

    public static final AvailableModules source = AvailableModules.CLOCK;

    private LED phase1, phase2;
    private PushBtn resetBtn;
    private int step = 0;

    private boolean resetting = false;

    Clock() {
        w = 50;
        h = 100;

        addOutput("Phase 1",  25, Port.CLOCK);
        addOutput("Phase 2", -25, Port.CLOCK);

        phase1 = new LED(-10, -25, LEDColour.GREEN);
        phase2 = new LED(-10,  25, LEDColour.GREEN);
        resetBtn = new PushBtn(10, 25);
        addPart(phase1);
        addPart(phase2);
        addPart(resetBtn);
    }

    @Override
    public BaseModule createNew() {
        return new Clock();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawOutputs(g);

        drawStaticParts(g);

        // Show label
        g.setColor(Colors.moduleLabel);
        g.setFont(Fonts.moduleLabel);
        g.drawString("CLK", -18, 8);
    }

    /**
     * <p>"Ticks" the clock to its next step.
     * The change needs to be explicitly propagated afterwards.</p>
     * <ul>
     * <li>0 - x</li>
     * <li>1 - Phase 1</li>
     * <li>2 - x</li>
     * <li>3 - Phase 3</li>
     * </ul>
     */
    public void tick() {
        step++;
        if (step > 3) step = 0;
    }

    @Override
    public void propagate() {
        // Reset mechanism
        boolean sendReset;
        if (resetBtn.getEnabled()) {
            sendReset = true;
            step = 0;
        }
        else {
            sendReset = false;
        }

        // Phase 1
        BinData p1 = new BinData();
        p1.setBooleanBit(0, step == 1);
        phase1.setEnabled(step == 1);

        // Phase 2
        BinData p2 = new BinData();
        p2.setBooleanBit(0, step == 3);
        phase2.setEnabled(step == 3);

        // Reset signal
        p2.setBooleanBit(1, sendReset);
        p1.setBooleanBit(1, sendReset);

        // Enable signal
        p2.setBooleanBit(2, true);
        p1.setBooleanBit(2, true);

        // Set the outputs
        outputs.get(0).setVal(p1);
        outputs.get(1).setVal(p2);
    }

    @Override
    public void dataIn(HashMap<String, String> data) {
        super.dataIn(data);

        if (data.containsKey("clock_phase")) {
            String phaseStr = data.get("clock_phase");
            try {
                step = Integer.parseInt(phaseStr);
            } catch (NumberFormatException e) {
                System.err.println("Warning: unable to parse clock_phase:");
                e.printStackTrace();
            }
        }
    }

    @Override
    public HashMap<String, String> dataOut() {
        HashMap<String, String> dataMap = super.dataOut();
        dataMap.put("clock_phase", String.valueOf(step));
        return dataMap;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.CLOCK;
    }

}
