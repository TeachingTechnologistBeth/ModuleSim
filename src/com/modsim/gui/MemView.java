package com.modsim.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.modsim.modules.NRAM;
import com.modsim.res.Colors;

public class MemView extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int adrW = 70;
    private static final int minCellW = 40;
    private static final int rowH = 20;

    private final MemEdit editor;

    private int updAdr = -1;
    private int offset = 0;

    public MemView(MemEdit editor) {
        this.editor = editor;
        this.setFocusable(true);
    }

    public void setOffset(int row) {
        offset = row;
    }

    public void setUpdated(int adr) {
        updAdr = adr;
    }

    @Override
    public void paintComponent(Graphics oldG) {
        Graphics2D g = (Graphics2D) oldG;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int rowsToDraw = getHeight()/rowH + 1;

        for (int i = offset; i < offset+rowsToDraw; i++) {
            drawRow(g, i, i*getCellsPerRow());
            g.translate(0, rowH);
        }
    }

    /**
     * @return The number of cells per row
     */
    public int getCellsPerRow() {
        int w = getWidth();
        int available = w - adrW;
        return Math.max(1, available / minCellW);
    }

    /**
     * @return The full row count for all addresses in memory
     */
    public int getRowCount() {
        int cells = NRAM.MAX_ADDR;
        return cells/getCellsPerRow();
    }

    /**
     * Draws a row of memory cells, including a start address
     * @param g - Graphics context
     */
    private void drawRow(Graphics2D g, int row, int startAdr) {
        AffineTransform xForm = g.getTransform();
        g.setColor(Color.WHITE);

        if (startAdr <= NRAM.MAX_ADDR) {
            // Draw the address
            g.fillRect(0, 0, adrW, rowH);

            g.setColor(Color.BLACK);
            g.drawString("0x"+String.format("%04x", startAdr).toUpperCase(), 5, 14);

            // Draw the cells
            int val = (row % 2 == 1 ? 230 : 238);
            g.setColor(new Color(val, val, val));
            g.fillRect(adrW, 0, getWidth()-adrW, rowH);

            int cells = getCellsPerRow();
            double cellW = (double)(getWidth()-adrW) / cells;

            g.translate(adrW, 0);
            for (int i = 0; i < cells; i++) {
                drawCell(g, startAdr + i, cellW);
                g.translate(cellW, 0.0);
            }
            g.setTransform(xForm);

            // Divider line
            g.setColor(Colors.moduleLabel);
            g.drawLine(0, rowH-1, getWidth(), rowH-1);
        }
        else {
            g.fillRect(0, 0, getWidth(), rowH);
        }
    }

    /**
     * Draws a memory cell - a single byte
     */
    private void drawCell(Graphics2D g, int adr, double w) {
        if (adr > NRAM.MAX_ADDR) return;

        g.setColor(Color.BLUE);

        if (adr == updAdr) {
            g.fillRect(0, 0, (int) w, rowH);
            g.setColor(Color.WHITE);
        }

        int val = editor.getByte(adr);
        g.drawString(String.format("%02x", val).toUpperCase(), (int) w/2 - 5, 14);
    }

}
