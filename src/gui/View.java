package gui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;

import modules.BaseModule;
import modules.Link;
import simulator.Main;
import simulator.PickableEntity;
import tools.BaseTool;
import tools.OperationStack;
import tools.PlaceTool;
import util.ModuleClipboard;
import util.Vec2;

/**
 * The main viewport for the simulator
 * @author aw12700
 *
 */
public class View extends JPanel {

    public int zoomI = 3;
    public double zoom = 0.6;

    public double camX = 0, camY = 0;
    public AffineTransform wToV = new AffineTransform();

    private static final long serialVersionUID = 1L;
    public BaseTool curTool = null;
    public OperationStack opStack = new OperationStack();
    public ModuleClipboard clipboard = new ModuleClipboard();

    public List<PickableEntity> selection = new ArrayList<PickableEntity>();
    
    public boolean quality = true;

    public View() {
        setFocusable(true);
        ViewUtil listener = new ViewUtil();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addMouseWheelListener(listener);
        addKeyListener(listener);
    }

    public void calcXForm() {
        wToV = new AffineTransform();

        wToV.translate(camX, camY);
        wToV.translate((getWidth() / 2), (getHeight() / 2));

        wToV.scale(zoom, zoom);
    }

    @Override
    public void paintComponent(Graphics oldG) {
        Graphics2D g = (Graphics2D) oldG;

        // Antialiasing
        if (quality) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // View transform
        calcXForm();

        AffineTransform old = new AffineTransform(g.getTransform());

        // Fill back
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Grid
        g.setColor(new Color(230,230,230));
        double xD = (camX + getWidth()/2);
        double yD = (camY + getHeight()/2);
        double xOff = (int)xD % (int)(Main.sim.grid * zoom);
        double yOff = (int)yD % (int)(Main.sim.grid * zoom);
        g.translate(xOff, yOff);
        drawGrid(g);

        g.setTransform(old);

        // Draw links
        g.transform(wToV);
        synchronized (Main.sim) {
            for (Link l : Main.sim.getLinks()) {
                if (l == null) {
                    System.err.println("Warning: Null link encountered while drawing");
                    continue;
                }
                l.draw(g);
            }

            g.setTransform(old);

            // Draw modules
            for (BaseModule m : Main.sim.getModules()) {
                m.updateXForm();
                g.transform(m.toView);
                m.paint(g);
                
                if (m.error) {
                    drawError(g);
                }
                
                g.setTransform(old);
            }

            for (BaseModule m : Main.sim.getModules()) {
                if (m.selected) {
                    g.transform(m.toView);
                    m.drawBounds(g);
                    g.setTransform(old);
                }
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
     * Draws an error flag
     */
    public void drawError(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval(-30, -30, 60, 60);
        g.setColor(Color.WHITE);
        g.fillOval(-25, -25, 50, 50);
        g.setColor(Color.RED);
        g.fillRect(-3, -16, 6, 20);
        g.fillOval(-3, 8, 6, 6);
    }
    
    /**
     * Draws a grid
     */
    public void drawGrid(Graphics2D g) {
        double grid = (int)(Main.sim.grid * zoom);

        int xNum = (int)(getWidth() / grid);
        int yNum = (int)(getHeight() / grid);

        for (int i = 0; i <= xNum + 1; i++) {
            g.drawLine((int)(i * grid), (int)-grid, (int)(i*grid), getHeight() + (int)grid);
        }

        for (int i = 0; i <= yNum + 1; i++) {
            g.drawLine((int)-grid, (int)(i * grid), getWidth() + (int)grid, (int)(i*grid));
        }
    }

    // Selection management
    public void select(PickableEntity targ) {
        selection.remove(targ);
        selection.add(targ);
        targ.selected = true;
    }
    public void select(List<PickableEntity> entities) {
        for (PickableEntity e : entities) {
            selection.remove(e);
            selection.add(e);
            e.selected = true;
        }
    }

    public void toggleSelect(PickableEntity e) {
        if (selection.contains(e)) {
            selection.remove(e);
            e.selected = false;
        }
        else {
            selection.add(e);
            e.selected = true;
        }
    }

    public void deselect(BaseModule m) {
        selection.remove(m);
        m.selected = false;
    }

    public void clearSelect() {
        for (PickableEntity e : selection) {
            e.selected = false;
        }

        selection.clear();
    }

    public void deleteSelection() {
        opStack.beginCompoundOp();
        ArrayList<PickableEntity> forDeletion = new ArrayList<PickableEntity>(selection);
        for (PickableEntity e : forDeletion) {
            e.delete();
        }
        clearSelect();
        opStack.endCompoundOp();
    }
    
    public void copy(List<PickableEntity> entities) {
        clipboard.copy(entities);
    }
    
    public void paste() {
        if (curTool != null) curTool.cancel();
        curTool = new PlaceTool(clipboard);
    }

    public void undo() {
        System.out.println("Requested undo!");
        if (curTool == null) opStack.undo();
        else {
            curTool.cancel();
            curTool = null;
        }
    }

    public void redo() {
        System.out.println("Requested redo!");
        if (curTool == null) opStack.redo();
        else System.out.println("Cannot redo during tool use");
    }

    public void zoomIn(int x, int y) {
        Vec2 zmPt = ViewUtil.screenToWorld(new Vec2(x, y));

        zoomI++;
        if (zoomI > 12) {
            zoomI = 12;
        }
        else {
            zoom = zoomI * 0.2;
            calcXForm();
            Vec2 newScreenPt = ViewUtil.worldToScreen(zmPt);
            camX -= newScreenPt.x - x;
            camY -= newScreenPt.y - y;
        }
    }

    public void zoomOut(int x, int y) {
        Vec2 zmPt = ViewUtil.screenToWorld(new Vec2(x, y));

        zoomI--;
        if (zoomI < 1) {
            zoomI = 1;
        }
        else {
            zoom = zoomI * 0.2;
            calcXForm();
            Vec2 newScreenPt = ViewUtil.worldToScreen(zmPt);
            camX -= newScreenPt.x - x;
            camY -= newScreenPt.y - y;
        }
    }

}
