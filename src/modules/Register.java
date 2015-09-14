package modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import util.BinData;
import modules.parts.LEDRow;
import modules.parts.Port;

/**
 * Register module
 * @author aw12700
 *
 */
public class Register extends BaseModule {

    private LEDRow leds;
    private BinData myData = new BinData(0);

    Register() {
        w = 100;
        h = 50;

        addInput("Data in", 0, Port.DATA);
        addInput("Control in", 0, Port.CLK, new BinData(0, 1, 0, 0));

        addOutput("Data out", 0, Port.DATA);
        addOutput("Control out", 0, Port.CLK);

        leds = new LEDRow(25, -20);
        addPart(leds);
        
        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Register();
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100,100,100));
        drawBox(g, 10);

        // Show IO
        g.setColor(new Color(120,120,120));
        drawInputs(g);
        drawOutputs(g);

        drawParts(g);

        // Show label
        g.setColor(new Color(200,200,200));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("R", -7, 8);
    }

    @Override
    public void propagate() {
        // Get control input
        BinData cIn = inputs.get(1).getVal();

        boolean clk = cIn.getBit(0) == 1;
        boolean rst = cIn.getBit(1) == 1;
        boolean en  = cIn.getBit(2) == 1;

        // Get data input
        BinData data = inputs.get(0).getVal();

        // Store / reset the data
        if (rst) {
            myData.setInt(0);
        }
        else if (clk && en) {
            myData = new BinData(data);
        }

        // Show it
        leds.setVal(myData);

        // Set the outputs
        outputs.get(0).setVal(myData);
        outputs.get(1).setVal(cIn);
    }

    @Override
    protected void reset() {
        myData.setInt(0);
    }

    public BinData getStoredVal() {
        synchronized (this) {
            System.out.println("Register value retrieved: " + myData.toString());
            return new BinData(myData);
        }
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.REGISTER;
    }

}
