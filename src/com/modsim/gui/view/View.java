package com.modsim.gui.view;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.*;

import com.modsim.modules.BaseModule;
import com.modsim.modules.Link;
import com.modsim.modules.parts.VisiblePart;
import com.modsim.res.Colors;
import com.modsim.Main;
import com.modsim.tools.BaseTool;
import com.modsim.tools.PlaceTool;
import com.modsim.util.Vec2;

import static java.lang.Math.abs;

/**
 * The main viewport for the simulator
 * @author aw12700
 *
 */
public class View extends JPanel {

	public int init_zoomI = 3;
	public double init_camX = 0, init_camY = 0;
		
    public int zoomI = init_zoomI;
    public double zoom = zoomI * ZOOM_MULTIPLIER;

    public static final double ZOOM_MULTIPLIER = 0.15;
    public static final int ZOOM_LIMIT = 12;

    public double camX = init_camX, camY = init_camY;
    public AffineTransform wToV = new AffineTransform();

    private static final long serialVersionUID = 1L;
    public BaseTool curTool = null;

    public boolean useAA = true;

    private int dynamicRefreshRate = 30;

    private BufferedImage staticCanvas = null;
    private boolean staticIsDirty = true;
    private long lastDynamicPaint = 0;

    // Zoom caps
    public static final double minZoom = 0.01;
    public static final double maxZoom = 6.0;

    public View() {
        setFocusable(true);
        ViewUtil listener = new ViewUtil();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addMouseWheelListener(listener);
        addKeyListener(listener);

        // Fetch the preferred refresh rate
        Preferences prefs = Preferences.userNodeForPackage(View.class);
        dynamicRefreshRate = prefs.getInt("dynamic_refresh_rate", dynamicRefreshRate);
    }

    /***
     * @return The current dynamic refresh rate
     */
    public int getDynamicRefreshRate() {
        return dynamicRefreshRate;
    }

    /***
     * Sets and stores the dynamic refresh rate for the view. Capped to 5-120Hz
     * @param newRate The new refresh rate, in Hz
     */
    public void setDynamicRefreshRate(int newRate) {
        if (newRate < 5) {
            newRate = 5;
        }
        else if (newRate > 120) {
            newRate = 120;
        }
        Preferences prefs = Preferences.userNodeForPackage(View.class);
        dynamicRefreshRate = newRate;
        prefs.putInt("dynamic_refresh_rate", newRate);
    }

    public void calcXForm() {
        wToV = new AffineTransform();

        wToV.translate(camX, camY);
        wToV.translate((getWidth() / 2), (getHeight() / 2));

        wToV.scale(zoom, zoom);
    }

    public void paintStatic() {
        // Renders the static portion of the viewport
        if (staticCanvas == null || staticCanvas.getWidth() != getWidth() || staticCanvas.getHeight() != getHeight()) {
            staticCanvas = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());
            staticIsDirty = true;
        }
        Graphics2D staticG = staticCanvas.createGraphics();
        AffineTransform oldStatic = new AffineTransform(staticG.getTransform());

        if (staticIsDirty) {
            // Antialiasing
            if (useAA) {
                staticG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            else {
                staticG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            // Fill background
            staticG.setTransform(oldStatic);
            staticG.setColor(Colors.background);
            staticG.fillRect(0, 0, getWidth(), getHeight());
            // Grid
            staticG.setColor(Colors.grid);
            double xD = (camX + getWidth() / 2);
            double yD = (camY + getHeight() / 2);
            double xOff = xD % (Main.sim.grid * zoom);
            double yOff = yD % (Main.sim.grid * zoom);
            staticG.translate(xOff, yOff);
            drawGrid(staticG);
            staticG.setTransform(oldStatic);

            // Draw modules - static
            staticG.setTransform(oldStatic);
            for (BaseModule m : Main.sim.getModules()) {
                m.updateXForm();
                staticG.transform(m.toView);
                m.paintStatic(staticG);
                staticG.setTransform(oldStatic);
            }

            // Draw links
            staticG.transform(wToV);
            for (Link l : Main.sim.getLinks()) {
                if (l == null) {
                    System.err.println("Warning: Null link encountered while drawing");
                    continue;
                }
                l.draw(staticG);
            }
            
            staticG.setTransform(oldStatic);

            // Static canvas is now up-to-date
            staticIsDirty = false;
        }
    }

    @Override
    public void paintComponent(Graphics oldG) {
        lastDynamicPaint = System.currentTimeMillis();
        Graphics2D g = (Graphics2D) oldG;

        // Antialiasing
        if (useAA) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // Refresh the view's transform
        calcXForm();
        // Store the original view transform for restoration to a known state
        AffineTransform old = new AffineTransform(g.getTransform());

        // Static stuff is drawn below all dynamic stuff
        paintStatic();
        g.drawImage(staticCanvas, 0, 0, getWidth(), getHeight(), null);

        // Draw modules - dynamic
        for (BaseModule m : Main.sim.getModules()) {
            m.updateXForm();
            g.transform(m.toView);
            m.paintDynamic(g);

            if (m.error) {
                drawError(g);
            }

            g.setTransform(old);
        }

        // Labels are drawn over all module renderings
        for (BaseModule m : Main.sim.getModules()) {
            g.transform(m.toView);
            m.drawLabel(g);
            g.setTransform(old);
        }

        // Highlighted bounds are drawn over labels
        for (BaseModule m : Main.sim.getModules()) {
            if (m.selected) {
                g.transform(m.toView);
                m.drawBounds(g);
                g.setTransform(old);
            }
        }

        // Draw the tool
        if (curTool != null) {
            g.transform(wToV);
            curTool.paintWorld(g);
            g.setTransform(old);
            curTool.paintScreen(g);
        }

        g.setTransform(old);

        // Draw iterations per second
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        DecimalFormat df = new DecimalFormat("#.##");
        String num = df.format(Main.sim.itrPerSec);
        int pad = 20 - num.length();
        for (int i=0; i < pad; i++) num = " " + num;
        g.drawString(num + " iterations/s", 10, 10);
    }

    /**
     * Draws a module's error flag
     */
    private void drawError(Graphics2D g) {
        g.setColor(Colors.errorEdge);
        g.drawOval(-30, -30, 60, 60);
        g.setColor(Colors.errorFill);
        g.fillOval(-27, -27, 54, 54);
        g.setColor(Colors.errorText);
        g.fillRect(-3, -16, 6, 20);
        g.fillOval(-3, 8, 6, 6);
    }

    /**
     * Draws the background grid
     */
    private void drawGrid(Graphics2D g) {
        double grid = zoom * Main.sim.grid;

        // When extremely zoomed-out, displaying the grid is costly and pointless
        if (grid < 1.5) {
            return;
        }

        int xNum = (int)(getWidth() / grid);
        int yNum = (int)(getHeight() / grid);

        AffineTransform oldTransform = new AffineTransform(g.getTransform());
        Line2D verticalLine = new Line2D.Double(0.0, -grid, 0.0, getHeight() + grid);
        Line2D horizontalLine = new Line2D.Double(-grid, 0.0, getWidth() + grid, 0.0);
        for (int i = 0; i <= xNum + 1; i++) {
            g.draw(verticalLine);
            g.translate(grid, 0.0);
        }
        g.setTransform(oldTransform);
        for (int i = 0; i <= yNum + 1; i++) {
            g.draw(horizontalLine);
            g.translate(0.0, grid);
        }
        g.setTransform(oldTransform);
    }

    /**
     * Whether or not a tool is currently in use
     * @return True if tool is in use
     */
    public boolean hasTool() {
        return curTool != null;
    }

    /**
     * Cancels the current tool usage (if any)
     */
    public void cancelTool() {
        if (curTool != null) curTool.cancel();
        curTool = null;
    }

    /**
     * Sets the current tool
     * @param newTool New tool to use
     */
    public void setTool(BaseTool newTool) {
        if (curTool != null) curTool.cancel();
        curTool = newTool;
    }

    /**
     * Smoothly zooms the viewport in or out of the specified screen point
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public void zoom(int x, int y, double amount) {
        Vec2 zmPt = ViewUtil.screenToWorld(new Vec2(x, y), true);

        zoom -= zoom * amount * ZOOM_MULTIPLIER;
        if (zoom < minZoom) {
            zoom = minZoom;
        }
        else if (zoom > maxZoom) {
            zoom = maxZoom;
        }
        calcXForm();
        Vec2 newScreenPt = ViewUtil.worldToScreen(zmPt);
        camX -= newScreenPt.x - x;
        camY -= newScreenPt.y - y;
        calcXForm();
        //redraw
        flagStaticRedraw();
    }

    /***
     * Flags the static canvas as dirty, triggering a redraw of all static parts on the next view update
     */
    public void flagStaticRedraw() {
        staticIsDirty = true;
        repaint();
    }

    /***
     * "Soft" request for a redraw, used for simulation updates. Capped at 30Hz refresh rate.
     */
    public void flagDynamicRedraw() {
        long currentTime = System.currentTimeMillis();
        if (abs(currentTime - lastDynamicPaint) > (1000 / dynamicRefreshRate)) {
            repaint();
        }
        else {
            // persistence-of-vision simulation
            for (BaseModule m : Main.sim.getModules()) {
                for (VisiblePart p : m.parts) {
                    p.povTick();
                }
            }
        }
    }

	public void resetView() {
		//center view
		camX = init_camX;
		camY = init_camY;
		//reset zoom
		zoomI = init_zoomI;
	    zoom = zoomI * ZOOM_MULTIPLIER;
        //redraw
        flagStaticRedraw();
	}
}
