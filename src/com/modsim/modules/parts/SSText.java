package com.modsim.modules.parts;

import com.modsim.res.Fonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class SSText extends VisiblePart {

    private String text;
    private Font font;
    private Color color;
    private RefreshMode refreshMode = RefreshMode.Static;

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

    public SSText(int x, int y, String txt, int fontSize, Color col, RefreshMode refreshMode) {
        this.x = x;
        this.y = y;
        text = txt;
        font = new Font(Fonts.moduleLabel.getFamily(),
                Fonts.moduleLabel.getStyle(),
                fontSize);
        color = col;
        this.refreshMode = refreshMode;
    }

    public SSText(int x, int y, String txt, int fontSize, Color col, String font_name) {
        this.x = x;
        this.y = y;
        text = txt;
        font = new Font(font_name,
                Fonts.moduleLabel.getStyle(),
                fontSize);
        color = col;
    }

    public SSText(int x, int y, String txt, int fontSize, Color col, String font_name, RefreshMode refreshMode) {
        this.x = x;
        this.y = y;
        text = txt;
        font = new Font(font_name,
                Fonts.moduleLabel.getStyle(),
                fontSize);
        color = col;
        this.refreshMode = refreshMode;
    }

    public void setText(String value) {
        text = value;
    }

    public void setRefreshMode(RefreshMode value) {
        refreshMode = value;
    }

    @Override
    public void paint(Graphics2D g) {
        // Draw the text
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    public void povTick() {
        // Don't care
    }

    @Override
    public RefreshMode getRefreshMode() {
        return refreshMode;
    }
}
