package modules.parts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Label extends VisiblePart {

    private String text;
    private Font font;
    private Color color;
    
    /**
     * Create a label
     * @param x
     * @param y
     * @param txt Text to display
     * @param fontSize Size of label font
     * @param col Color of label font
     */
    public Label(int x, int y, String txt, int fontSize, Color col) {
        this.x = x;
        this.y = y;
        text = txt;
        font = new Font("Sans Serif", Font.BOLD, fontSize);
        color = col;
    }
    
    /**
     * Create a label
     * @param x
     * @param y
     * @param txt Text to display
     * @param fontSize Size of label font
     */
    public Label(int x, int y, String txt, int fontSize) {
        this(x, y, txt, fontSize, Color.GRAY);
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
