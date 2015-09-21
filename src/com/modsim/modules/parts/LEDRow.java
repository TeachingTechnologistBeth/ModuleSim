package com.modsim.modules.parts;

import java.awt.Color;
import java.awt.Graphics2D;

import util.BinData;

/**
 * Row of four LEDs (for data display)
 * @author aw12700
 *
 */
public class LEDRow extends VisiblePart {

	private Color color = Color.BLUE;
	private Color hColor = Color.BLUE;

	private volatile BinData curVal = new BinData(0);

    // Convenience method, LEDRow is usually data.
    public LEDRow(int x, int y) {
        this(x, y, LEDColour.RED);
    }

	public LEDRow(int x, int y, LEDColour col) {
		this.x = x;
		this.y = y;

        color = col.light;
        hColor = col.highlight;
	}

	/**
	 * Set next value to display
	 * @param v
	 */
	public void setVal(BinData v) {
		curVal.set(v);
	}

	/**
	 * Get current display value
	 * @return
	 */
	public BinData getVal() {
		return curVal;
	}

	@Override
	public void paint(Graphics2D g) {
		// Draw the LEDs
		g.setColor(new Color(80,80,80));
		g.fillRect(x-15, y-3, 30, 6);

		BinData v = getVal();
		for (int i = 0; i < 4; i++) {
			int offs = x+(1-i)*8;

			if (v.getBit(i) == 1) {
				g.setColor(color);
				g.fillRect(offs+1, y-3, 6, 6);
				g.setColor(hColor);
				g.fillRect(offs+2, y-2, 4, 4);
			}
			else {
				g.setColor(new Color(50,50,50));
				g.fillRect(offs+2, y-2, 4, 4);
			}
		}
	}

    @Override
    public void reset() {
        curVal = new BinData(0);
    }

}
