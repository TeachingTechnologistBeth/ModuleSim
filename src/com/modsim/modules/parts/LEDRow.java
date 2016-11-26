package com.modsim.modules.parts;

import java.awt.Color;
import java.awt.Graphics2D;

import com.modsim.res.Colors;
import com.modsim.res.Colors.LEDColour;
import com.modsim.util.BinData;

/**
 * Row of four LEDs (for data display)
 * @author aw12700
 *
 */
public class LEDRow extends VisiblePart {

	private static final int NLEDS = 4;

	private Color color = Color.BLUE;
	private Color hColor = Color.BLUE;

	private volatile BinData curVal = new BinData(0);

	private int povTicks = 0;
	private int povHits[] = new int[NLEDS];

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
		g.setColor(Colors.ledBack);
		g.fillRect(x-15, y-3, 30, 6);

		BinData v = getVal();
		for (int i = 0; i < NLEDS; i++) {
			int offs = x+(1-i)*8;

			if (v.getBit(i) == 1 || povHits[i] > 0) {
				g.setColor(color);
				g.fillRect(offs+1, y-3, 6, 6);
				g.setColor(hColor);
				g.fillRect(offs+2, y-2, 4, 4);
			}
			else {
				g.setColor(Colors.ledOff);
				g.fillRect(offs+2, y-2, 4, 4);
			}
		}

		resetPov();
	}

	@Override
	public void povTick() {
		povTicks++;
		BinData v = getVal();
		for (int i = 0; i < NLEDS; i++) {
			if (v.getBit(i) == 1) {
				povHits[i]++;
			}
		}
	}

	private void resetPov() {
		povTicks = 0;
		for (int i = 0; i < NLEDS; i++) {
			povHits[i] = 0;
		}
	}

    @Override
	public RefreshMode getRefreshMode() {
		return RefreshMode.Dynamic;
	}
}
