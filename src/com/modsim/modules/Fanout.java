package com.modsim.modules;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.modsim.res.Colors;
import com.modsim.res.Colors.LEDColour;

import com.modsim.util.BezierCurve;
import com.modsim.util.Vec2;
import com.modsim.modules.parts.LEDRow;
import com.modsim.modules.ports.Output;
import com.modsim.modules.parts.Port;

/**
 * Fanout module
 *
 * @author aw12700
 *
 */
public class Fanout extends BaseModule {

    private final LEDRow dLEDs;
    private final List<BezierCurve> curves;

    Fanout() {
        w = 150;
        h = 50;

        // Outputs
        addOutput("Output A", -50, Port.GENERIC);
        addOutput("Output B", -25, Port.GENERIC);
        addOutput("Output C", 25, Port.GENERIC);
        addOutput("Output D", 50, Port.GENERIC);

        // Input
        addInput("Input", 0, Port.GENERIC);

        dLEDs = new LEDRow(0, -20, LEDColour.YELLOW);
        addPart(dLEDs);

        // Drawing
        BezierCurve[] cs = new BezierCurve[4];
        for (int i = 0; i < cs.length; i++) {
            double x = 25 * (i - 2 + (i > 1 ? 1 : 0));
            cs[i] = new BezierCurve(new Vec2(0, 23), new Vec2(x, -25),
                                    new Vec2(x, 0),  new Vec2(x, 0));
        }
        curves = Collections.unmodifiableList(Arrays.asList(cs));
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Fanout();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);

        // Show output/input
        g.setColor(Colors.modulePorts);
        drawOutputs(g);
        drawInputs(g);

        // Show drawing
        for (BezierCurve c : curves) {
            c.draw(g);
        }

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        dLEDs.setVal(inputs.get(0).getVal());

        for (Output o : outputs) {
            o.setVal(inputs.get(0).getVal());
        }
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.FANOUT;
    }
}
