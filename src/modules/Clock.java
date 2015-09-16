package modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import util.BinData;
import modules.parts.LED;
import modules.parts.LEDColour;
import modules.parts.Port;
import modules.parts.PushBtn;

/**
 * Clock module
 * @author aw12700
 *
 */
public class Clock extends BaseModule {

    public static final AvailableModules source = AvailableModules.CLOCK;

    private LED phase1, phase2;
    private PushBtn rstBtn;
    private int step = 0;

    Clock() {
        w = 50;
        h = 100;

        addOutput("Phase 1",  25, Port.CLK);
        addOutput("Phase 2", -25, Port.CLK);

        phase1 = new LED(-10, -25, LEDColour.GREEN);
        phase2 = new LED(-10,  25, LEDColour.GREEN);
        rstBtn = new PushBtn(10, 25);
        addPart(phase1);
        addPart(phase2);
        addPart(rstBtn);
    }

    @Override
    public BaseModule createNew() {
        return new Clock();
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100,100,100));
        drawBox(g, 10);

        // Show IO
        g.setColor(new Color(120,120,120));
        drawOutputs(g);

        drawParts(g);

        // Show label
        g.setColor(new Color(200,200,200));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
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
        // Phase 1
        BinData p1 = new BinData();
        p1.setBit(0, (step == 1) ? 1 : 0);
        p1.setBit(2, 1);
        phase1.setEnabled(step == 1);

        // Phase 2
        BinData p2 = new BinData();
        p2.setBit(0, (step == 3) ? 1 : 0);
        p2.setBit(2, 1);
        phase2.setEnabled(step == 3);

        // Reset
        p2.setBooleanBit(1, rstBtn.getEnabled());
        p1.setBooleanBit(1, rstBtn.getEnabled());

        // Set the outputs
        outputs.get(0).setVal(p1);
        outputs.get(1).setVal(p2);
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.CLOCK;
    }

}
