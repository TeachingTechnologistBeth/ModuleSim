package com.modsim.res;

import com.modsim.modules.parts.Port;

import java.awt.*;

/**
 * Created by awick on 21/09/2015.
 */
public class Colors {

    /**
     * Defines colours to be used for LEDs and related displays.
     * @author gf12815
     */
    public enum LEDColour {
        RED(new Color(255, 80, 80), new Color(255, 180, 180)),
        BLUE(new Color(50, 50, 255), new Color(180, 180, 255)),
        GREEN(new Color(80, 255, 80), new Color(180, 255, 180)),
        YELLOW(new Color(220, 220, 80), new Color(255, 255, 180));

        LEDColour(Color lt, Color hlt) {
            light = lt;
            highlight = hlt;
        }

        public final Color light;
        public final Color highlight;
    }

    // Background colors
    public static final Color background = new Color(255, 255, 255);
    public static final Color grid = new Color(230,230,230);


    // Module colors
    public static final Color moduleFill = new Color(100, 100, 100);
    public static final Color moduleInset = new Color(80, 80, 80);
    public static final Color moduleLabel = new Color(200, 200, 200);
    public static final Color modulePorts = new Color(120, 120, 120);
    public static final Color moduleSubLabel = new Color(120, 120, 120);
    
    public static final Color splitMergeLabel = new Color(60, 60, 60);
    
    public static final Color errorEdge = new Color(255, 0, 0, 120);
    public static final Color errorFill = new Color(255, 255, 255, 60);
    public static final Color errorText = new Color(255, 10, 10);

    // Labels
    public static final Color labelFill = new Color(230, 230, 230, 200);
    public static final Color labelText = new Color(0, 0, 0, 200);

    // LED/part colors
    public static final Color ledBack = new Color(0, 0, 0, 60);
    public static final Color ledOff = new Color(50, 50, 50);
    public static final Color buttonTop = new Color(150, 150, 150);
    public static final Color button = new Color(130, 130, 130);


    // Link/port colors by type
    public static final Color selectedLink = new Color(200, 200, 100);
    public static final Color[] links;
    static {
        links = new Color[4];
        links[Port.GENERIC] = Color.GRAY;
        links[Port.CLOCK] = new Color(100, 160, 100);
        links[Port.CTRL] = Color.blue;
        links[Port.DATA] = Color.red;
    }

    // Blended link/port colors by type
    public static final Color[][] blendedLinks;
    static {
        blendedLinks = new Color[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Color c0 = links[i];
                Color c1 = links[j];
                double totalAlpha = c0.getAlpha() + c1.getAlpha();
                double weight0 = c0.getAlpha() / totalAlpha;
                double weight1 = c1.getAlpha() / totalAlpha;

                double r = weight0 * c0.getRed() + weight1 * c1.getRed();
                double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
                double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
                double a = Math.max(c0.getAlpha(), c1.getAlpha());

                blendedLinks[i][j] = new Color((int) r, (int) g, (int) b, (int) a);
            }
        }
    }

}
