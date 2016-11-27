package com.modsim.modules.parts;

import java.awt.Color;

import com.modsim.res.Colors;
import com.modsim.res.Colors.LEDColour;
import java.awt.Graphics2D;

public class LED extends TogglePart {

	private Color color;
	private Color hColor;

	private int povTicks = 0;
	private int povHits = 0;

    // Convenience method, single LEDs are most commonly control.
    public LED(int x, int y) {
        this(x, y, LEDColour.BLUE);
    }

	public LED(int x, int y, LEDColour col) {
		this.x = x;
		this.y = y;

        color = col.light;
        hColor = col.highlight;
	}

	@Override
	public void paint(Graphics2D g) {
		// Display the LED
		g.setColor(Colors.ledBack);
		g.fillRect(x-4, y-4, 8, 8);

		if (getEnabled() || povHits > 0) {
			g.setColor(color);
            g.fillRect(x-4, y-4, 8, 8);
			g.setColor(hColor);
			g.fillRect(x-2, y-2, 4, 4);
		}
		else {
			g.setColor(Colors.ledOff);
			g.fillRect(x-2, y-2, 4, 4);
		}

		povTicks = 0;
        povHits = 0;
	}

	@Override
	public void povTick() {
		povTicks++;
		if (getEnabled()) {
			povHits++;
		}
	}
}
