package com.modsim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JOptionPane;

import com.modsim.modules.NRAM;
import com.modsim.Main;

public class HexReader {

    /**
     * Reads a hex format file
     */
    public static void readFile(File hexFile, NRAM nram) {
        if (nram == null) {
            JOptionPane.showMessageDialog(null, "No NRAM module present");
            return;
        }

        try {
            nram.clear();
            BufferedReader in = new BufferedReader(new FileReader(hexFile));

            // Read in the full file
            String line;
            String file = "";
            while ((line = in.readLine()) != null) {
                file += line + " ";
            }
            in.close();

            readString(file, nram);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to read hex file: " + e.getMessage());
        }
    }

    public static void readString(String store, NRAM nram) {
        try {
            String[] chopped = store.split("( |\t)+");

            int adr = 0;
            for (String s : chopped) {
                // Skip isEmpty strings
                if (s.equals("")) continue;

                // Multiple entries
                int rpt = 1;
                if (s.contains("x")) {
                    String[] pair = s.split("x");
                    rpt = Integer.parseInt(pair[0]);
                    s = pair[1];
                }

                // Flag invalid formatting
                if (s.length() != 2) {
                    throw new Exception("File load requires hex codes 2 digits long");
                }

                int n1 = Integer.parseInt(s.substring(0, 1), 16);
                int n2 = Integer.parseInt(s.substring(1), 16);

                // Support repeated entries
                for (int i = 0; i < rpt; i++) {
                    nram.write(adr, new BinData(n2), new BinData(n1));
                    adr++;
                }
            }

            // Propagate change
            Main.sim.propagate(nram);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Invalid hex string: "+e.getMessage());
        }
    }

}
