package com.modsim.modules.parts;

import com.modsim.res.Fonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class SSText extends VisiblePart {

    private String text;
    private Font font;
    private Color color;

    /**
     * Create a label
     * @param x X offset from centre
     * @param y Y offset from centre
     * @param txt Text to display
     * @param fontSize Size of label font
     * @param col Color of label font
     */
    public SSText(int x, int y, String txt, Color col) {
        this.x = x;
        this.y = y;
        text = txt;
        font = Fonts.moduleLabel;
        color = col;
    }

    /**
     * Create a label
     * @param x X offset from centre
     * @param y Y offset from centre
     * @param txt Text to display
     * @param fontSize Size of label font
     * @param col Color of label font
     */
    public SSText(int x, int y, String txt, int fontSize, Color col) {
        this.x = x;
        this.y = y;
        text = txt;
        font = new Font(Fonts.moduleLabel.getFamily(),
                Fonts.moduleLabel.getStyle(),
                fontSize);
        color = col;
    }

    @Override
    public void paint(Graphics2D g) {
        // Draw the text
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    public void reset() {
        // Noop
    }

}
