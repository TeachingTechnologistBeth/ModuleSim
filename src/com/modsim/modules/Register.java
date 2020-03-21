package com.modsim.modules;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;
import com.modsim.res.Colors;
import com.modsim.res.Fonts;
import com.modsim.util.BinData;
import com.modsim.modules.parts.LEDRow;
import com.modsim.modules.parts.Port;

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
        controlIn   = addInput("Control in", 0, Port.CLOCK, new BinData(0, 1, 0, 1));

        dataOut     = addOutput("Data out", 0, Port.DATA);
        controlOut  = addOutput("Control out", 0, Port.CLOCK);

        ledRow = new LEDRow(25, -20);
        addPart(ledRow);

        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new Register();
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

        // Show label
        g.setColor(Colors.moduleLabel);
        g.setFont(Fonts.moduleLabel);
        g.drawString("R", -7, 8);

        drawStaticParts(g);
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
        HashMap<String, String> dataMap = super.dataOut();
        String latched = myData.toString();
        dataMap.put("latched_value", latched);
        return dataMap;
    }

    @Override
    public void dataIn(HashMap<String, String> data) {
        super.dataIn(data);

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
