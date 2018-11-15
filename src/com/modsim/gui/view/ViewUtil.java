package com.modsim.gui.view;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import com.modsim.Main;
import com.modsim.modules.*;
import com.modsim.modules.ports.BidirPort;
import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;
import com.modsim.modules.parts.Port;
import com.modsim.simulator.*;
import com.modsim.tools.*;
import com.modsim.util.Path;
import com.modsim.util.Vec2;

/**
 * Listens for mouse events on the view.
 * Handles most user interaction with the view via the com.modsim.tools system.
 * @author aw12700
 *
 */
public class ViewUtil implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    private double cStartX, cStartY;
    private double oldCX, oldCY;
    private boolean camDrag = false;

    public static boolean snap = true;
    public static int snapGridSize = 25;

    /**
     * Finds a clicked link by approximate (binary-search) closest point on curve
     * @param pt World-space point to check
     * @return Link at point, or null if there is none
     */
    public static Link worldSpace_linkAt(Vec2 pt) {
        for (Link link : Main.sim.getLinks()) {
            Path.PointInfo info = link.path.approxClosestPoint(pt, 6);

            if (info.dist < 15.0) {
                return link;
            }
        }

        return null;
    }

    /**
     * Tests collision with ports
     * @param x Screen-space x coord
     * @param y Screen-space y coord
     * @return The port selected, or null if there was none
     */
    public static Port screenSpace_portAt(double x, double y) {
        double portR = 10;

        synchronized (Main.sim) {
            int i = Main.sim.getModules().size() - 1;
            for (; i >= 0; i--) {
                BaseModule m = Main.sim.getModules().get(i);

                double[] pt = {x, y};

                // Get clicked point in object space
                try {m.toView.inverseTransform(pt, 0, pt, 0, 1);}
                catch (Exception e) {
                    System.err.println("Non invertible transform");
                }

                Vec2 v = new Vec2(pt);

                // Test outputs
                for (Output o : m.outputs) {
                    Vec2 p = new Vec2();

                    if (o.type == Port.CTRL || o.type == Port.CLOCK) {
                        p.x = -m.w / 2;
                        p.y = -o.pos;
                    }
                    else {
                        p.x = o.pos;
                        p.y = -m.h / 2;
                    }

                    if (v.dist(p) < portR) {
                        return o;
                    }
                }

                // Test Inputs
                for (Input in : m.inputs) {
                    Vec2 p = new Vec2();

                    if (in.type == Port.CTRL || in.type == Port.CLOCK) {
                        p.x = m.w / 2;
                        p.y = -in.pos;
                    }
                    else {
                        p.x = in.pos;
                        p.y = m.h / 2;
                    }

                    if (v.dist(p) < portR) {
                        return in;
                    }
                }

                // Test Bidir
                for (BidirPort bd : m.bidirs) {
                    Vec2 p = new Vec2();

                    if (bd.type == Port.CTRL || bd.type == Port.CLOCK) {
                        p.x = bd.side * m.w / 2;
                        p.y = -bd.pos;
                    }
                    else {
                        p.x = bd.pos;
                        p.y = bd.side * m.h / 2;
                    }

                    if (v.dist(p) < portR) {
                        return bd;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tests collision with entities
     * @param x Screen-space x coord
     * @param y Screen-space y coord
     * @return The picked entity, or null if the background was clicked
     */
    public static PickableEntity screenSpace_entityAt(double x, double y) {
        double[] pt = {x, y};

        try {Main.ui.view.wToV.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {
            System.err.println("Non invertible transform");
            return null;
        }

        Vec2 clickPt = new Vec2(pt);

        // Loop the entities
        for (PickableEntity e : Main.sim.getEntities()) {
            if (e.intersects(clickPt)) {
                return e;
            }
        }

        return null;
    }

    /**
     * Find entities within the specified area

     * @return
     */
    public static List<PickableEntity> worldSpace_entitiesWithin(double x1, double y1, double x2, double y2) {
        List<PickableEntity> result = new ArrayList<PickableEntity>();

        // Loop the entities
        for (PickableEntity e : Main.sim.getEntities()) {
            if (e.within(x1, y1, x2, y2)) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * Find entities within the specified area
     * @param x X coord of rect in screen space
     * @param y Y coord of rect in screen space
     * @param w width of rect
     * @param h height of rect
     * @return
     */
    public static List<PickableEntity> screenSpace_entitiesWithin(double x, double y, double x2, double y2) {
        List<PickableEntity> result = new ArrayList<PickableEntity>();

        double[] pt = {x, y, x2, y2};

        try {Main.ui.view.wToV.inverseTransform(pt, 0, pt, 0, 2);}
        catch (Exception e) {
            System.err.println("Non invertible transform");
        }

        x = pt[0];
        y = pt[1];
        x2 = pt[2];
        y2 = pt[3];

        // Loop the entities
        for (PickableEntity e : Main.sim.getEntities()) {
            if (e.within(x, y, x2, y2)) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * Adjusts an on-screen point to world-space
     */
    public static Vec2 screenToWorld(Vec2 p, boolean noSnap) {
        double[] pt = p.asArray();

        try {Main.ui.view.wToV.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {
            return null;
        }

        if (snap && !noSnap) {
            pt[0] = snapGridSize*(Math.round(pt[0]/snapGridSize));
            pt[1] = snapGridSize*(Math.round(pt[1]/snapGridSize));
        }

        return new Vec2(pt);
    }

    /**
     * Adjusts a world point to screen-space
     */
    public static Vec2 worldToScreen(Vec2 p) {
        double[] pt = p.asArray();

        Main.ui.view.wToV.transform(pt, 0, pt, 0, 1);

        return new Vec2(pt);
    }

    public void mouseClicked(MouseEvent e) {
        testKeys(e);
        BaseTool tool = Main.ui.view.curTool;

        if (e.getButton() == MouseEvent.BUTTON3) {
            // Do nothing if the tool handles things
            if (tool != null && (tool.handlesRbDown() || tool.handlesRbUp())) return;

            // Right-click
            PickableEntity targ = screenSpace_entityAt(e.getX(), e.getY());

            if (targ != null) {
                Main.selection.add(targ);
            }

            Main.selection.showContextMenu(e.getX(), e.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
        Main.ui.view.requestFocusInWindow();
    }

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        testKeys(e);
        BaseTool tool = Main.ui.view.curTool;

        if (e.getButton() == MouseEvent.BUTTON1) {
            // Left click handled by tools
            PickableEntity targ = screenSpace_entityAt(e.getX(), e.getY());

            // See if module handles interaction - otherwise, use tools
            boolean handled = false;
            if (targ != null && targ.getType() == PickableEntity.MODULE) {
                BaseModule m = (BaseModule) targ;
                handled = m.lbDown(e.getX(), e.getY(), e.isShiftDown());
            }

            if (!handled) {
                if (tool != null) {
                    Main.ui.view.curTool = tool.lbDown(e.getX(), e.getY(), e.isShiftDown());
                }
                else {
                    Port p = screenSpace_portAt(e.getX(), e.getY());

                    //Link behaviour
                    if (p != null) {
                        Main.selection.clear();

                        tool = new MakeLinkTool();
                        Main.ui.view.curTool = tool.lbDown(e.getX(), e.getY(), e.isShiftDown());
                    }
                    else {
                        // Link edit if we haven't hit a module
                        if (targ == null) {
                            Vec2 worldSpace = screenToWorld(new Vec2(e.getX(), e.getY()) , true);
                            Link l = worldSpace_linkAt(worldSpace);

                            if (l != null) {
                                Main.selection.clear();

                                tool = new EditLinkTool(l);
                                Main.ui.view.curTool = tool.mouseMove(e.getX(), e.getY());
                                return;
                            }
                        }

                        // Finally, try selection behaviour
                        tool = new SelectTool();
                        Main.ui.view.curTool = tool.lbDown(e.getX(), e.getY(), e.isShiftDown());
                    }
                }
            }
        }
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (tool != null && tool.handlesRbDown()) {
                Main.ui.view.curTool = tool.rbDown(e.getX(), e.getY());
            }

            // Camera behaviour
            cStartX = e.getX();
            cStartY = e.getY();
            oldCX = Main.ui.view.camX;
            oldCY = Main.ui.view.camY;
            camDrag = true;
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void mouseReleased(MouseEvent e) {
        testKeys(e);
        BaseTool tool = Main.ui.view.curTool;

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (tool != null) {
                Main.ui.view.curTool = tool.lbUp(e.getX(), e.getY());
            }
            else {
                PickableEntity targ = screenSpace_entityAt(e.getX(), e.getY());

                if (targ == null) {
                    Main.selection.clear();
                }
                else if (targ.getType() == PickableEntity.MODULE) {
                    BaseModule m = (BaseModule) targ;
                    m.lbUp(e.getX(), e.getY());
                }
            }
        }
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (tool != null && tool.handlesRbUp()) {
                Main.ui.view.curTool = tool.rbUp(e.getX(), e.getY());
            }

            camDrag = false;
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void mouseDragged(MouseEvent e) {
        testKeys(e);
        BaseTool tool = Main.ui.view.curTool;

        if (camDrag) {
            Main.ui.view.camX = oldCX + e.getX() - cStartX;
            Main.ui.view.camY = oldCY + e.getY() - cStartY;
        }
        else if (tool != null) {
            Main.ui.view.curTool = tool.mouseDrag(e.getX(), e.getY());
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void mouseMoved(MouseEvent e) {
        testKeys(e);
        BaseTool tool = Main.ui.view.curTool;

        Port p = screenSpace_portAt(e.getX(), e.getY());
        if (p != null) {
            if (p.hasDirection()) {
                Main.ui.view.setToolTipText(p.text + " - " + p.getVal().toString() + (p.canOutput() ? " OUT" : " IN"));
            }
            else {
                Main.ui.view.setToolTipText(p.text + " - Disconnected");
            }
        }
        else {
            Main.ui.view.setToolTipText(null);
        }

        if (tool != null) {
            Main.ui.view.curTool = tool.mouseMove(e.getX(), e.getY());
        }

        Main.ui.view.flagDynamicRedraw();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        testKeys(e);
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            Main.ui.view.zoom(e.getX(), e.getY(), e.getPreciseWheelRotation());
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void keyPressed(KeyEvent e) {
        View v = Main.ui.view;

        // Cancel tool usage on escape press
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (v.curTool != null) {
                v.curTool.cancel();
                v.curTool = null;
            }
        }
        // Delete selection
        else if ((   e.getKeyCode() == KeyEvent.VK_DELETE
                  || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                  || e.getKeyCode() == KeyEvent.VK_X)
                 && !Main.selection.isEmpty()) {
            Main.selection.deleteAll();
        }
        // Pass to tool
        else if (v.curTool != null) {
            v.curTool = v.curTool.keyDown(e.getKeyCode());
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void keyReleased(KeyEvent e) {
        View v = Main.ui.view;
        if (v.curTool != null) {
            v.curTool.keyUp(e.getKeyCode());
        }

        Main.ui.view.flagStaticRedraw();
    }

    public void keyTyped(KeyEvent e) {}

    public void testKeys(MouseEvent e) {
        BaseTool.CTRL = e.isControlDown();
        BaseTool.SHIFT = e.isShiftDown();
    }

}
