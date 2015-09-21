package com.modsim.modules.parts;

import java.awt.Color;

/**
 * Defines colours to be used for LEDs and related displays.
 * @author gf12815
 */
public enum LEDColour {
    RED(new Color(255, 80, 80), new Color(255, 180, 180)),
    BLUE(new Color(50, 50, 255), new Color(180, 180, 255)),
    GREEN(new Color(80, 255, 80), new Color(180, 255, 180)),
    YELLOW(new Color(220, 220, 80), new Color(255, 255, 180));

    private LEDColour(Color lt, Color hlt) {
        light = lt;
        highlight = hlt;
    }

    public final Color light;
    public final Color highlight;
}
