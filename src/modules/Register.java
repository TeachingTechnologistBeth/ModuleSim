package modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modules.parts.Input;
import modules.parts.Output;
import util.BinData;
import modules.parts.LEDRow;
import modules.parts.Port;

import javax.swing.*;

/**
 * Register module
 * @author aw12700
 *
 */
public class Register extends BaseModule {

    private LEDRow ledRow;
    private BinData myData = new BinData(0);

    private final Input dataIn;
    private final Output dataOut;
    private final Input controlIn;
    private final Output controlOut;

    Register() {
        w = 100;
        h = 50;

        dataIn      = addInput("Data in", 0, Port.DATA);
        controlIn   = addInput("Control in", 0, Port.CLK, new BinData(0, 1, 0, 0));

        dataOut     = addOutput("Data out", 0, Port.DATA);
        controlOut  = addOutput("Control out", 0, Port.CLK);

        ledRow = new LEDRow(25, -20);
        addPart(ledRow);

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
        BinData controlVal = controlIn.getVal();

        boolean clk = controlVal.getBooleanBit(0);
        boolean rst = controlVal.getBooleanBit(1);
        boolean en  = controlVal.getBooleanBit(2);

        // Store / reset the data
        if (rst) {
            myData.setInt(0);
        }
        else if (clk && en) {
            myData = dataIn.getVal();
        }

        // Show it
        ledRow.setVal(myData);

        // Set the outputs
        dataOut.setVal(myData);
        controlOut.setVal(controlVal);
    }

    public void setStoredVal(BinData val) {
        myData.set(val);
    }

    public BinData getStoredVal() {
        return new BinData(myData);
    }

    public void clear() {
        myData = new BinData(0);
    }

    @Override
    public HashMap<String, String> dataOut() {
        HashMap<String, String> dataMap = new HashMap<>();
        String latched = myData.toString();
        dataMap.put("latched_value", latched);
        return dataMap;
    }

    @Override
    public void dataIn(HashMap<String, String> data) {
        if (data.containsKey("latched_value")) {
            // Parse latched value
            String str = data.get("latched_value");
            try {
                if (str.length() != 4) throw new Exception("bad string length");
                // Who needs loops right?
                boolean b0 = Integer.parseInt(str.substring(0, 1)) == 1;
                boolean b1 = Integer.parseInt(str.substring(1, 2)) == 1;
                boolean b2 = Integer.parseInt(str.substring(2, 3)) == 1;
                boolean b3 = Integer.parseInt(str.substring(3)) == 1;
                myData = new BinData(b0, b1, b2, b3); // note the order!
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Corrupt/unrecognized SwitchInput data: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> outPorts = new ArrayList<>();

        // Control is passed-through, but data is affected by either input
        if (in == controlIn) {
            outPorts.add(dataOut);
            outPorts.add(controlOut);
        }
        else if (in == dataIn) {
            outPorts.add(dataOut);
        }

        return outPorts;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.REGISTER;
    }

}
