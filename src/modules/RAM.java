package modules;

import gui.MemEdit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import modules.parts.Input;
import modules.parts.LED;
import modules.parts.LEDRow;
import modules.parts.Output;
import modules.parts.Port;
import org.w3c.dom.Element;
import util.BinData;
import util.HexReader;
import util.HexWriter;

/**
 * RAM Module Holds data and instructions for designs
 *
 * @author aw12700
 *
 */
public class RAM extends BaseModule {

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
    private final Input dInA;
    private final Input dInB;
    private final Input contIn;
    private final Input addrA;
    private final Input addrB;
    private final Input addrC;
    private final Input addrD;
    private final Output dOutA;
    private final Output dOutB;
    private final Output contOut;

    private final LEDRow dLEDA;
    private final LEDRow dLEDB;
    private final LED cLED;

    RAM(boolean cleared) {
        w = 150;
        h = 200;

        // Data in
        dInB = addInput("Data in B (High bits)", -25, Port.DATA);
        dInA = addInput("Data in A (Low bits)", 25, Port.DATA);

        // Control
        contIn = addInput("Control in", -75, Port.CLK);

        // Address in
        addrA = addInput("Address in A (Lowest Bits)", 50, Port.CTRL);
        addrB = addInput("Address in B", 25, Port.CTRL);
        addrC = addInput("Address in C", 0, Port.CTRL);
        addrD = addInput("Address in D (Highest Bits)", -25, Port.CTRL);

        // Data out
        dOutB = addOutput("Data out B (High bits)", -25, Port.DATA);
        dOutA = addOutput("Data out A (Low bits)", 25, Port.DATA);

        // Control out
        contOut = addOutput("Control out", 0, Port.CLK);

        clearOnReset = cleared;
        reset();

        dLEDA = new LEDRow(25, -80);
        addPart(dLEDA);
        dLEDB = new LEDRow(-25, -80);
        addPart(dLEDB);

        // Add display
        cLED = new LED(50, 50);
        addPart(cLED);

        propagate();
    }

    @Override
    public BaseModule createNew() {
        return new RAM(true);
    }

    @Override
    public void paint(Graphics2D g) {
        // Fill in polygon
        g.setColor(new Color(100, 100, 100));
        drawBox(g, 10);

        // Show IO
        g.setColor(new Color(120, 120, 120));
        drawInputs(g);
        drawOutputs(g);

        // Show LEDs
        drawParts(g);

        // Show label
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        g.drawString("RAM", -40, 15);
    }

    @Override
    public void propagate() {
        int address = combineAddress(addrA.getVal(), addrB.getVal(), addrC.getVal(), addrD.getVal());
        contOut.setVal(contIn.getVal());

        if (contIn.getVal().getBit(WRITE_PIN) == BinData.HIGH) {
            // Write high, disable output
            dOutA.setVal(new BinData());
            dOutB.setVal(new BinData());
            dLEDA.setVal(dOutA.getVal());
            dLEDB.setVal(dOutB.getVal());

            if (contIn.getVal().getBit(CLOCK_PIN) == BinData.HIGH) {
                // Clock high, write to memory.
                write(address, dInA.getVal(), dInB.getVal());
            }
        } else {
            // Write low, enable output
            BinData[] read = read(address);
            dOutA.setVal(read[0]);
            dOutB.setVal(read[1]);
            dLEDA.setVal(read[0]);
            dLEDB.setVal(read[1]);
        }

        cLED.setEnabled(contIn.getVal().getBit(WRITE_PIN) == BinData.HIGH);
    }

    public void clear() {
        final BinData blank = new BinData(BinData.LOW, BinData.LOW, BinData.LOW, BinData.LOW);

        for (int i = 0; i < LOCATIONS; i++) {
            // Perhaps we should lazily initialize the data - complicates memory editor.
            // Alternatively, make BinData immutable?
            store[i] = new BinData(blank);
        }
    }

    @Override
    public void dataIn(HashMap<String, String> data) {
        if (data.containsKey("memory_store")) {
            String storeStr = data.get("memory_store");
            HexReader.readString(storeStr, this);
        }
    }

    @Override
    public HashMap<String, String> dataOut() {
        String storeStr = HexWriter.hexString(this, false);
        if (storeStr.isEmpty()) return null;

        HashMap<String, String> data = new HashMap<>();
        data.put("memory_store", storeStr);
        return data;
    }

    @Override
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

    public void write(int address, BinData d0, BinData d1) {
        if (address <= MAX_ADDR) {
            address = address << 1;

            store[address] = d0;
            store[address | 1] = d1;

            // Update the memory editor, if any
            if (editor != null) {
                editor.updAdr = address >> 1;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editor.update();
                    }
                });
            }
        } else {
            Logger.getLogger(RAM.class.getName()).warning("RAM tile index out of bounds.");
        }
    }

    public BinData[] read(int address) {
        if (address <= MAX_ADDR) {
            address = address << 1;

            return new BinData[]{store[address], store[address | 1]};
        } else {
            Logger.getLogger(RAM.class.getName()).warning("RAM tile index out of bounds.");
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
    public AvailableModules getModType() {
        return AvailableModules.RAM;
    }
}
