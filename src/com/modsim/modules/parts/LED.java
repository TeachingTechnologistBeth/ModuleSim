package com.modsim.modules.parts;

import java.awt.Color;
import java.awt.Graphics2D;

public class LED extends TogglePart {

	private Color color;
	private Color hColor;

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
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x-4, y-4, 8, 8);

		if (getEnabled()) {
			g.setColor(color);
			g.fillRect(x-4, y-4, 8, 8);
			g.setColor(hColor);
			g.fillRect(x-2, y-2, 4, 4);
		}
		else {
			g.setColor(new Color(50,50,50));
			g.fillRect(x-2, y-2, 4, 4);
		}

	}

}
