package com.modsim.modules;

import com.modsim.gui.MemEdit;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.modsim.modules.parts.*;
import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;
import com.modsim.res.Colors;
import com.modsim.res.Colors.LEDColour;
import com.modsim.util.BinData;
import com.modsim.util.HexReader;
import com.modsim.util.HexWriter;

/**
 * NRAM Module Holds data and instructions for designs
 *
 * @author aw12700
 *
 */
public class NRAM extends BaseModule {

    private MemEdit editor = null;

    /**
     * The maximum VALID address
     */
    public static final int MAX_ADDR = 0xFFFF;

    // The number of BinData locations in the store
    private static final int LOCATIONS = 0x20000;
    private static final int CLOCK_PIN = 0;
    private static final int WRITE_PIN = 2;

    private final BinData[] store = new BinData[LOCATIONS];
    private final boolean clearOnReset;

    // Port definitions
    private final Input dataInA;
    private final Input dataInB;
    private final Input controlIn;
    private final Input addrA;
    private final Input addrB;
    private final Input addrC;
    private final Input addrD;
    private final Output dataOutA;
    private final Output dataOutB;
    private final Output controlOut;

    private final LEDRow dataLEDsA;
    private final LEDRow dataLEDsB;
    private final LED writeLED;

    private final Switch writeJumper;

    NRAM(boolean cleared) {
        w = 150;
        h = 200;

        // Data in
        dataInB = addInput("Data in B (High bits)", -25, Port.DATA);
        dataInA = addInput("Data in A (Low bits)", 25, Port.DATA);

        // Control
        controlIn = addInput("Control in", -75, Port.CLOCK);

        // Address in
        addrA = addInput("Address in A (Lowest Bits)", 50, Port.CTRL);
        addrB = addInput("Address in B", 25, Port.CTRL);
        addrC = addInput("Address in C", 0, Port.CTRL);
        addrD = addInput("Address in D (Highest Bits)", -25, Port.CTRL);

        // Data out
        dataOutB = addOutput("Data out B (High bits)", -25, Port.DATA);
        dataOutA = addOutput("Data out A (Low bits)", 25, Port.DATA);

        // Control out
        controlOut = addOutput("Control out", 0, Port.CLOCK);

        clearOnReset = cleared;
        reset();

        dataLEDsA = new LEDRow(25, -80);
        addPart(dataLEDsA);
        dataLEDsB = new LEDRow(-25, -80);
        addPart(dataLEDsB);

        // Add display
        writeLED = new LED(50, 50);
        addPart(writeLED);

        // The write jumper
        writeJumper = new Switch(-50, 50, LEDColour.BLUE);
        addPart(writeJumper);
        addPart(new SSText(10, 53, "WRITE", 10, Colors.moduleSubLabel));

        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new NRAM(true);
    }

    @Override
    public void paintStatic(Graphics2D g) {
        // Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);
        // 'Control' area
        g.setColor(Colors.moduleInset);
        drawTrapezoid(g, 10, 0, 65, 130, 70);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

        // Show label
        g.setColor(Colors.moduleLabel);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        g.drawString("NRAM", -58, 15);

        drawStaticParts(g);
    }

    @Override
    public void propagate() {
        int address = combineAddress(addrA.getVal(), addrB.getVal(), addrC.getVal(), addrD.getVal());
        controlOut.setVal(controlIn.getVal()); // pass-through
        writeLED.setEnabled(false);

        if (controlIn.getVal().getBit(WRITE_PIN) == BinData.HIGH) {
            // Write high, disable output
            dataOutA.setVal(new BinData());
            dataOutB.setVal(new BinData());
            dataLEDsA.setVal(dataOutA.getVal());
            dataLEDsB.setVal(dataOutB.getVal());

            // Clock high AND jumper on, write to memory.
            if (controlIn.getVal().getBit(CLOCK_PIN) == BinData.HIGH && writeJumper.getEnabled()) {
                write(address, dataInA.getVal(), dataInB.getVal());
                writeLED.setEnabled(true);
            }
        } else {
            // Write low, enable output
            BinData[] read = read(address);
            dataOutA.setVal(read[0]);
            dataOutB.setVal(read[1]);
            dataLEDsA.setVal(read[0]);
            dataLEDsB.setVal(read[1]);
        }
    }

    public void clear() {
        final BinData blank = new BinData(BinData.LOW, BinData.LOW, BinData.LOW, BinData.LOW);

        for (int i = 0; i < LOCATIONS; i++) {
            // Perhaps we should lazily initialize the data - complicates memory editor.
            // Alternatively, make BinData immutable?
            store[i] = new BinData(blank);
        }

        updateEditor(0);
    }

    @Override
    public void dataIn(HashMap<String, String> data) {
        super.dataIn(data);

        if (data.containsKey("memory_store")) {
            String storeStr = data.get("memory_store");
            HexReader.readString(storeStr, this);
        }

        if (data.containsKey("write_jumper")) {
            String jumperStr = data.get("write_jumper");
            writeJumper.setEnabled(jumperStr.equals("1"));
        }
        else {
            writeJumper.setEnabled(true);
        }
    }

    @Override
    public HashMap<String, String> dataOut() {
        String storeStr = HexWriter.hexString(this, false);
        if (storeStr.isEmpty()) return super.dataOut();

        HashMap<String, String> data = super.dataOut();
        data.put("memory_store", storeStr);
        data.put("write_jumper", (writeJumper.getEnabled()) ? "1" : "0");

        return data;
    }

    protected final void reset() {
        // Initialise all values. Use 0 for simplistic view, or random to reflect
        // the undefined state of memory when first powered up.
        if (clearOnReset) {
            clear();
        } else {
            Random rng = new Random();

            for (int i = 0; i < LOCATIONS; i++) {
                store[i] = new BinData();
                store[i].setUInt(rng.nextInt());
            }
        }
    }

    private static int combineAddress(BinData a0, BinData a1, BinData a2, BinData a3) {
        return a0.getUInt() | (a1.getUInt() << 4) | (a2.getUInt() << 8) | (a3.getUInt() << 12);
    }

    private void updateEditor(int address) {
        // Update the memory editor, if any
        if (editor != null) {
            editor.updAdr = address >> 1;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editor.update();
                }
            });
        }
    }

    public void write(int address, BinData d0, BinData d1) {
        if (address <= MAX_ADDR) {
            address = address << 1;

            store[address] = d0;
            store[address | 1] = d1;

            updateEditor(address);
        } else {
            Logger.getLogger(NRAM.class.getName()).warning("NRAM tile index out of bounds.");
        }
    }

    public BinData[] read(int address) {
        if (address <= MAX_ADDR) {
            address = address << 1;

            return new BinData[]{store[address], store[address | 1]};
        } else {
            Logger.getLogger(NRAM.class.getName()).warning("NRAM tile index out of bounds.");
            return new BinData[]{new BinData(), new BinData()};
        }
    }

    /**
     * Attaches an editor to the module
     * @param e Editor to attach
     */
    public void attachEditor(MemEdit e) {
        editor = e;
    }

    @Override
    public List<Port> getAffected(Port in) {
        List<Port> outPorts = new ArrayList<>();

        // Data out can only be affected by control input (including addresses)
        if (    in == addrA || in == addrB ||
                in == addrC || in == addrD ||
                in == controlIn) {
            outPorts.add(dataOutA);
            outPorts.add(dataOutB);
        }

        // Control out is only affected by control in (pass-through)
        if (in == controlIn) {
            outPorts.add(controlOut);
        }

        // Note: data-in never directly affects any outputs!
        return outPorts;
    }

    @Override
    public AvailableModules getModType() {
        return AvailableModules.RAM;
    }
}
