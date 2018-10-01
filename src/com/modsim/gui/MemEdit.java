package com.modsim.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.modsim.Main;
import com.modsim.util.BinData;
import com.modsim.util.HexReader;
import com.modsim.util.HexWriter;
import com.modsim.modules.NRAM;
import com.modsim.util.XMLReader;

public class MemEdit {

    private NRAM nram = null;

    public final JDialog frame = new JDialog(Main.ui.frame, "Memory Viewer", Dialog.ModalityType.MODELESS);
    private final JMenuBar menu = new JMenuBar();
    private final FilenameFilter hexFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".hex");
        }
    };
    private final JScrollBar scroll = new JScrollBar(JScrollBar.VERTICAL);
    private final JTextField jumpAdr = new JTextField();

    private final MemView memView;

    public int updAdr = -1;

    /**
     * Creates the memory editor. 'show()' must be called before it becomes visible.
     */
    public MemEdit() {
        addFileMenu();
        frame.setJMenuBar(menu);

        JPanel adrStrip = new JPanel();
        adrStrip.setLayout(new BoxLayout(adrStrip, BoxLayout.LINE_AXIS));
        adrStrip.add(new JLabel("  Jump to: "));
        adrStrip.add(jumpAdr);

        JButton goBtn = new JButton("Go");
        JButton lastBtn = new JButton("Last Changed");
        adrStrip.add(goBtn);
        adrStrip.add(lastBtn);

        frame.add(adrStrip, BorderLayout.NORTH);

        frame.add(scroll, BorderLayout.LINE_END);
        memView = new MemView(this);

        scroll.addAdjustmentListener(new ScrollAdjustmentListener());

        memView.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                scroll.setMaximum(memView.getRowCount());
            }
        });
        memView.addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                int shift = e.getUnitsToScroll();
                scroll.setValue(scroll.getValue() + shift);
            }
        });

        ActionListener jumpListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int adr;

                try {
                    adr = Integer.parseInt(jumpAdr.getText(), 16);
                }
                catch (NumberFormatException nfe) {
                    return; // Fail silently.
                }

                jumpTo(adr);
                //memView.setUpdated(adr);
                memView.repaint();
            }
        };

        jumpAdr.addActionListener(jumpListener);
        goBtn.addActionListener(jumpListener);

        lastBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (updAdr != -1) jumpTo(updAdr);
            }
        });

        frame.add(memView);
    }

    /**
     * Displays the editor for the given NRAM module
     */
    public void show(NRAM nram) {
        nram.attachEditor(this);
        frame.setSize(300, 700);
        frame.setLocation(800, 100);
        frame.setVisible(true);
        this.nram = nram;

        update();
    }

    /**
     * Hides the window
     */
    public void close() {
        frame.setVisible(false);
    }

    /**
     * Updates the view of the memory contents
     */
    public void update() {
        frame.setTitle("NRAM " + nram.label);
        memView.setUpdated(updAdr);
        memView.repaint();
    }

    /**
     * Gets a byte of memory from the attached NRAM module as an int
     * @param adr The address to fetch from
     * @return The stored byte
     */
    public int getByte(int adr) {
        BinData[] bits = nram.read(adr);
        return (bits[1].getUInt() << 4) | (bits[0].getUInt());
    }

    /**
     * Places the specified address in view
     * @param adr Address to jump to
     */
    public void jumpTo(int adr) {
        scroll.setValue(adr / memView.getCellsPerRow());
    }

    /**
     * Handles a save action to save the data
     * @param e The event that triggered the action
     * @param shouldClose Whether the window should close after a successful save
     */
    private void saveActionPerformed(ActionEvent e, boolean shouldClose) {
        Preferences prefs = Preferences.userNodeForPackage(MemEdit.class);
        FileDialog fd = new FileDialog(frame, "Save Hex-encoded data", FileDialog.SAVE);
        fd.setFilenameFilter(hexFilter);
        // can just append .hex if the user doesn't
        if (Main.sim.filePath.isEmpty()) {
            fd.setFile("*.hex");
        } else {
            int ind = Main.sim.filePath.lastIndexOf('/');
            fd.setFile(Main.sim.filePath.substring(ind + 1));
        }

        fd.setDirectory(prefs.get("hex_fileDir", ""));
        fd.setVisible(true);

        if (fd.getFile() != null) {
            String path = fd.getDirectory() + fd.getFile();

            // Is the file being created with the correct extension?
            if (!path.endsWith(".hex")) {
                path = path + ".hex";
            }

            HexWriter.writeFile(new File(path), nram);

            if (shouldClose) {
                close();
            }
        }
    }

    /**
     * Fills the file menu
     */
    private void addFileMenu() {
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem menuItem = new JMenuItem("Load Data");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.setToolTipText("Load a hex data file into the module (replaces the current contents)");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Preferences prefs = Preferences.userNodeForPackage(MemEdit.class);
                FileDialog fd = new FileDialog(frame, "Load Hex-encoded data", FileDialog.LOAD);

                fd.setFilenameFilter(hexFilter);
                fd.setFile("*.hex"); // FilenameFilter doesn't work on Windows

                fd.setDirectory(prefs.get("hex_fileDir", ""));
                fd.setVisible(true);

                if (fd.getFile() != null) {
                    String path = fd.getDirectory() + fd.getFile();

                    // Loop till we get a valid input
                    while (!path.endsWith(".hex")) {
                        int res = JOptionPane.showConfirmDialog(frame, "Data load: Warning",
                                "That doesn't appear to be a hex file. Try and open anyway?",
                                JOptionPane.YES_NO_CANCEL_OPTION);

                        if (res == JOptionPane.YES_OPTION) break;
                        else if (res == JOptionPane.NO_OPTION) {
                            fd.setFile("*.hex");
                            fd.setVisible(true);

                            if (fd.getFile() == null) return;
                            path = fd.getDirectory() + fd.getFile();
                        }
                        else {
                            return;
                        }
                    }

                    prefs.put("hex_fileDir", fd.getDirectory());
                    File file = new File(path);
                    HexReader.readFile(file, nram);
                    updAdr = -1;
                    update();
                }
            }
        });
        file.add(menuItem);

        menuItem = new JMenuItem("Save Data");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setToolTipText("Saves the current NRAM contents to a hex data file");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveActionPerformed(e, false);
            }
        });
        file.add(menuItem);

        menuItem = new JMenuItem("Close without save");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.setToolTipText("Closes the window without saving NRAM contents");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        file.add(menuItem);

        menuItem = new JMenuItem("Close with save");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.setToolTipText("Saves the current NRAM contents to a hex data file and closes the window");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveActionPerformed(e, true);
            }
        });
        file.add(menuItem);

        menu.add(file);
    }

    class ScrollAdjustmentListener implements AdjustmentListener {
        public void adjustmentValueChanged(AdjustmentEvent e) {
            memView.setOffset(e.getValue());
            memView.repaint();
        }
    }
}
