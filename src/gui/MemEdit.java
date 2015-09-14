package gui;

import java.awt.BorderLayout;
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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import simulator.Main;
import util.BinData;
import util.HexReader;
import util.HexWriter;
import modules.RAM;

public class MemEdit {
    
    private RAM targ = null;
    
    public final JDialog frame = new JDialog(Main.ui.frame, "Memory Viewer");
    private final JMenuBar menu = new JMenuBar();
    private final JFileChooser filePick = new JFileChooser();
    private final FileNameExtensionFilter hexFilter = new FileNameExtensionFilter("Hex files", "hex");
    private final JScrollBar scroll = new JScrollBar(JScrollBar.VERTICAL);
    private final JTextField jumpadr = new JTextField();
    
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
        adrStrip.add(jumpadr);
        
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
                    adr = Integer.parseInt(jumpadr.getText(), 16);
                }
                catch (NumberFormatException nfe) {
                    return; // Fail silently.
                }
                
                jumpTo(adr);
                //memView.setUpdated(adr);
                memView.repaint();
            }
        };
        
        jumpadr.addActionListener(jumpListener);
        goBtn.addActionListener(jumpListener);
        
        lastBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (updAdr != -1) jumpTo(updAdr);
            }
        });

        frame.add(memView);
    }
    
    /**
     * Displays the editor for the given RAM module
     */
    public void show(RAM ram) {
        ram.attachEditor(this);
        frame.setSize(300, 700);
        frame.setLocation(800, 100);
        frame.setVisible(true);
        targ = ram;
        
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
        memView.setUpdated(updAdr);
        memView.repaint();
    }
    
    /**
     * Gets a byte of memory from the attached RAM module as an int
     * @param adr The address to fetch from
     * @return The stored byte
     */
    public int getByte(int adr) {
        BinData[] bits = targ.read(adr);
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
                filePick.setFileFilter(hexFilter);
                int r = filePick.showOpenDialog(frame);

                if (r == JFileChooser.APPROVE_OPTION) {
                    File file = filePick.getSelectedFile();
                    HexReader.readFile(file, targ);
                    updAdr = -1;
                    update();
                }
            }
        });
        file.add(menuItem);
        
        menuItem = new JMenuItem("Save Data");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.setToolTipText("Saves the current RAM contents to a hex data file");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filePick.setFileFilter(hexFilter);
                int r = filePick.showSaveDialog(frame);

                if (r == JFileChooser.APPROVE_OPTION) {
                    File file = filePick.getSelectedFile();
                    
                    // Is the file being created with the correct extension?
                    if (!file.getName().endsWith(".hex")) {
                        File extFile = new File(file.getPath() + ".hex");
                        if (! extFile.exists()) {
                            // Rename the destination file if it doesn't exist.
                            file = extFile;
                        }
                    }
                    
                    HexWriter.writeFile(file, targ);
                }
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
