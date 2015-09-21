package com.modsim.util;

import java.io.*;

import javax.swing.JOptionPane;

import com.modsim.modules.NRAM;

public class HexWriter {

    /**
     * Writes a hex format file
     */
    public static void writeFile(File hexFile, NRAM nram) {
        if (nram == null) {
            JOptionPane.showMessageDialog(null, "No NRAM module present");
            return;
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(hexFile));
            out.write(hexString(nram, true));
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Hex file IO failure: "+e.getMessage());
        }
    }

    /**
     * Writes a hex format string containing the raw data from the specified NRAM module
     * @param nram NRAM module to read from
     * @return String of hex data
     */
    public static String hexString(NRAM nram, boolean splitLines) {
        if (nram == null) {
            return "";
        }

        try {
            StringWriter out = new StringWriter();

            int adr, entries = 0;
            for (adr = 0; adr <= NRAM.MAX_ADDR; adr++) {
                int val, newVal;
                int num = 0;
                int seekAdr = adr;

                BinData[] bits = nram.read(adr);
                newVal = bits[0].getUInt() | (bits[1].getUInt() << 4);

                // Seek out repeated bytes
                do {
                    seekAdr++;
                    val = newVal;

                    if (seekAdr <= NRAM.MAX_ADDR) {
                        bits = nram.read(seekAdr);
                        newVal = bits[0].getUInt() | (bits[1].getUInt() << 4);
                    }
                    else newVal = 0;

                    num++;
                } while (newVal == val && seekAdr < NRAM.MAX_ADDR);
                adr = seekAdr - 1;

                if (seekAdr == NRAM.MAX_ADDR && val == 0) {
                    continue; // No point writing trailing zeroes
                }

                // Combination of multiple bytes into one entry
                if (num > 1) {
                    out.write("" + num + "x");
                }

                // The byte value in hex format
                out.write(String.format("%02x\t", val));
                entries++;

                if (splitLines && entries % 8 == 0) {
                    out.write("\n");
                }
            }

            out.close();
            return out.getBuffer().toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to write hex file: " + e.getMessage());
            return "";
        }
    }

}
