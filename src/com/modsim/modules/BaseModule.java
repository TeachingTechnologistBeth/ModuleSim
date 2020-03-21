package com.modsim.modules;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.modsim.Main;
import com.modsim.modules.parts.*;
import com.modsim.modules.ports.BidirPort;
import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;
import com.modsim.res.Colors;
import com.modsim.res.Fonts;
import com.modsim.simulator.*;
import com.modsim.operations.DeleteOperation;
import com.modsim.util.BinData;
import com.modsim.util.Vec2;

/**
 * Base for module classes
 * @author aw12700
 *
 */
public abstract class BaseModule extends PickableEntity {

    public enum rotationDir {
        ROT_CW,
        ROT_CCW,
        ROT_180
    }

    public double w = 30, h = 30;
    public AffineTransform toWorld = new AffineTransform();
    public AffineTransform toView = new AffineTransform();

    public int orientation = 0;
    public String label = "";
    public int labelSize = 0;

    public List<Port> ports = new ArrayList<>();
    public List<Input> inputs = new ArrayList<>();
    public List<Output> outputs = new ArrayList<>();
    public List<BidirPort> bidirs = new ArrayList<>();
    public List<VisiblePart> parts = new ArrayList<>();

    public int ID;

    /**
     * Flag used to provide visual error feedback
      */
    public boolean error = false;

    /**
     * Get the object's ID, used for file operations
     * @return The ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Get the module's type as an AvailableModules enumeration
     * @return The module's type
     */
    public abstract AvailableModules getModType();

    /**
     * Adds a new bi-directional port on the input (bottom/right) side
     * @param name User-readable name
     * @param pos Offset from centre
     * @param type Data, control, clock or generic
     * @return The new port
     */
    public BidirPort addBidirInput(String name, int pos, int type) {
        BidirPort p = new BidirPort(1);
        p.text = name;
        p.pos = pos;
        p.type = type;
        p.owner = this;

        bidirs.add(p);
        ports.add(p);

        return p;
    }

    /**
     * Adds a new bi-directional port on the output (top/left) side
     * @param name User-readable name
     * @param pos Offset from centre
     * @param type Data, control, clock or generic
     * @return The new port
     */
    public BidirPort addBidirOutput(String name, int pos, int type) {
        BidirPort p = new BidirPort(-1);
        p.text = name;
        p.pos = pos;
        p.type = type;
        p.owner = this;

        bidirs.add(p);
        ports.add(p);

        return p;
    }

    /**
     * Adds an output
     * @param name User-readable name
     * @param pos Offset from centre
     * @param type Data, control, clock or generic
     * @return The new output
     */
    public Output addOutput(String name, int pos, int type) {
        Output o = new Output();
        o.text = name;
        o.pos = pos;
        o.type = type;
        o.owner = this;

        outputs.add(o);
        ports.add(o);
        return o;
    }

    /**
     * Adds an input
     * @param name User-readable name
     * @param pos Offset from centre
     * @param type Data, control, clock or generic
     * @return The new input
     */
    public Input addInput(String name, int pos, int type) {
        return addInput(name, pos, type, new BinData(0));
    }

    /**
     * Adds an input with a set pull value
     * @param name User-readable name
     * @param pos Offset from centre
     * @param type Data, control, clock or generic
     * @param pullVal 4-bit Binary pull value
     * @return The new input
     */
    public Input addInput(String name, int pos, int type, BinData pullVal) {
        Input i = new Input();
        i.text = name;
        i.pos = pos;
        i.type = type;
        i.owner = this;
        i.pull = pullVal;


        inputs.add(i);
        ports.add(i);
        return i;
    }

    /**
     * Returns ports affected by changes to the given input.
     * Should be overwritten by subclasses to improve loop detector accuracy.
     * @param in Input port to be changed
     */
    public List<Port> getAffected(Port in) {
        List<Port> outList = new ArrayList<>();
        if (in.canInput()) {
            for (Port p : ports) {
                if (p != in && p.canOutput()) {
                    outList.add(p);
                }
            }
        }

        return outList;
    }


    /**
     * Adds a part
     * @param p Part to add
     */
    public void addPart(VisiblePart p) {
        parts.add(p);
        p.owner = this;
    }

    /**
     * Draws static components for the module, in local space
     * @param g Graphics context to render with
     */
    public void paintStatic(Graphics2D g) {
        drawStaticParts(g);
    }

    /**
     * Draws dynamic (variable with module state) components for the module,
     * in local space.
     * @param g Graphics context to render with
     */
    public void paintDynamic(Graphics2D g) { drawDynamicParts(g); }

    /**
     * Displays the module's label in local space
     * @param g Graphics context to render with
     */
    public void drawLabel(Graphics2D g) {
        if (!label.isEmpty()) {
            double height, margin;
            Font font;
            if (labelSize == 0) {
                // Small
                height = 27;
                margin = 20;
                font = Fonts.label;
            }
            else {
                // Big
                height = 54;
                margin = 40;
                font = Fonts.bigLabel;
            }

            // Text sizing/centering
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D r = fm.getStringBounds(label, g);
            float x = (float) ((w - r.getWidth()) / 2 - w/2);
            float y = (float) (((height - r.getHeight()) / 2 + fm.getDescent()) + h/2 + 25 + height/2);

            g.setColor(Colors.labelFill);
            drawBox(g, 5, 0, (int) (h / 2 + 25 + height/2), (int) Math.max(w, r.getWidth() + margin), (int) height);

            g.setColor(Colors.labelText);
            g.drawString(label, x, y);
        }
    }

    /**
     * Displays the module's bounding box
     * @param g Graphics context to render with
     */
    @Override
    public void drawBounds(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2));
        g.drawRect((int)(- w/2) - 2, (int)(- h/2) - 2, (int)w + 2, (int)h + 2);
    }

    /**
     * Draws the dynamically variable visible parts, typically LEDs and switches
     * @param g Graphics context to render with
     */
    protected void drawDynamicParts(Graphics2D g) {
        for (VisiblePart p : parts) {
            if (p.getRefreshMode() == VisiblePart.RefreshMode.Dynamic) {
                p.paint(g);
            }
        }
    }

    /**
     * Draws static visible parts, typically labels
     * @param g Graphics context to render with
     */
    protected void drawStaticParts(Graphics2D g) {
        for (VisiblePart p : parts) {
            if (p.getRefreshMode() == VisiblePart.RefreshMode.Static) {
                p.paint(g);
            }
        }
    }

    /**
     * Draws the outputs as arrows
     * @param g Graphics context to render with
     */
    protected void drawOutputs(Graphics2D g) {
        g.setStroke(new BasicStroke(2));

        for (Output o : outputs) {
            boolean side = (o.type == Port.CTRL || o.type == Port.CLOCK);

            int aw = 10;
            int offset = o.pos;

            if (side) offset = - offset;

            int[] aPoints = {-aw + offset, -aw + offset, aw + offset, aw + offset, offset};

            // Base offset
            int base, angle;
            if (!side) {
                base = -(int)h/2;
                angle = 0;
            }
            else {
                base = -(int)w/2;
                angle = 90;
            }

            int[] bPoints = {base+aw, base, base, base+aw, base};

            // Draw internal shape
            if (!side)
                g.fillPolygon(aPoints, bPoints, 5);
            else
                g.fillPolygon(bPoints, aPoints, 5);

            Color oldC = g.getColor();

            // Color port by type
            g.setColor(Colors.links[o.type]);

            if (!side)
                g.fillArc(offset-5, base - 5, 10, 10, angle, 180);
            else
                g.fillArc(base - 5, offset - 5, 10, 10, angle, 180);

            g.setColor(oldC);
        }
    }

    /**
     * Draws the inputs as arrows
     * @param g Graphics context to render with
     */
    protected void drawInputs(Graphics2D g) {
        g.setStroke(new BasicStroke(2));

        // Loop the inputs
        for (Input i : inputs) {
            boolean side = (i.type == Port.CTRL || i.type == Port.CLOCK);

            int aw = 10;
            int offset = i.pos;

            if (side) offset = - offset;

            // Base offset
            int base, angle;
            if (!side) {
                base = (int)h/2;
                angle = 180;
            }
            else {
                base = (int)w/2;
                angle = 270;
            }

            int[] aPoints;
            int[] bPoints;
            int num;
            //if (i.bidir) {
            //    aPoints = new int[] {-aw + offset, -aw + offset, aw + offset, aw + offset, offset};
            //    bPoints = new int[] {base-aw, base, base, base-aw, base};
            //    num = 5;
            //}
            //else {
                aPoints = new int[]{-aw + offset, aw + offset, offset};
                bPoints = new int[]{base, base, base-aw};
                num = 3;
            //}

            // Draw internal shape
            if (!side)
                g.fillPolygon(aPoints, bPoints, num);
            else
                g.fillPolygon(bPoints, aPoints, num);

            Color oldC = g.getColor();

            // Color port by type
            g.setColor(Colors.links[i.type]);

            int x = offset-5;
            int y = base - 5;
            if (side) {
                int temp = x;
                x = y;
                y = temp;
            }

            //if (i.bidir)
            //    g.fillArc(x, y, 10, 10, angle, 180);
            //else
                g.drawArc(x, y, 10, 10, angle, 180);

            g.setColor(oldC);
        }
    }

    /**
     * Draws the bidirectional ports as arrows
     * @param g Graphics context to render with
     */
    protected void drawBidir(Graphics2D g) {
        g.setStroke(new BasicStroke(2));

        for (BidirPort bp : bidirs) {
            int aw = -10;
            int offset = bp.pos;

            // Base offset
            int base, angle;
            base = (int) h / 2;
            angle = (bp.side == 1) ? 180 : 0;
            base *= bp.side;

            // Points for output-style wedge
            int[] aPoints_wedge = {-aw + offset, -aw + offset, aw + offset, aw + offset, offset};
            int[] bPoints_wedge = {base + bp.side*aw, base, base, base + bp.side*aw, base};

            // Points for input-style arrow
            int[] aPoints_arrow = new int[]{offset - aw, offset + aw, offset};
            int[] bPoints_arrow = new int[]{base, base, base + bp.side*aw};

            // Draw internal shape
            if (bp.getMode() == Port.Mode.MODE_OUTPUT) {
                g.fillPolygon(aPoints_wedge, bPoints_wedge, 5);
            }
            else if (bp.getMode() == Port.Mode.MODE_INPUT) {
                g.fillPolygon(aPoints_arrow, bPoints_arrow, 3);
            }

            Color oldC = g.getColor();

            // Port color by type
            g.setColor(Colors.links[bp.type]);

            // Drawing style depends on port input/output mode
            if (bp.getMode() == Port.Mode.MODE_BIDIR || bp.getMode() == Port.Mode.MODE_OUTPUT) {
                g.fillArc(offset - 5, base - 5, 10, 10, angle, 180);
            } else {
                g.drawArc(offset - 5, base - 5, 10, 10, angle, 180);
            }

            g.setColor(oldC);
        }
    }

    /**
     * Draws the module as a trapezoid
     * @param g Graphics context to render with
     * @param corner Corner size in pixels
     */
    protected void drawTrapezoid(Graphics2D g, int corner) {
        drawTrapezoid(g, corner, 0, 0, (int) w, (int) h);
    }

    /**
     * Draws a trapezoid with the specified dimensions
     */
    protected void drawTrapezoid(Graphics2D g, int corner, int x, int y, int w, int h) {
        int[] xPoints = {x-w/2, x+w/2, x+w/2,        x+w/2-corner, x-w/2+corner, x-w/2};
        int[] yPoints = {y+h/2, y+h/2, y-h/2+corner, y-h/2,        y-h/2,        y-h/2 + corner};
        g.fillPolygon(xPoints, yPoints, 6);
    }

    /**
     * Draws the module as a box
     * @param g Graphics context to render with
     * @param corner Corner size in pixels
     */
    protected void drawBox(Graphics2D g, int corner) {
        drawBox(g, corner, 0, 0, (int) w, (int) h);
    }

    /**
     * Draws a box with the specified dimensions
     * @param g
     * @param corner
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected void drawBox(Graphics2D g, int corner, int x, int y, int width, int height) {
        int[] xPoints = {x-width/2, x-width/2+corner, x+width/2-corner, x+width/2,
                x+width/2, x+width/2-corner, x-width/2+corner, x-width/2};
        int[] yPoints = { y+height/2-corner, y+height/2, y+height/2, y+height/2-corner,
                y-height/2+corner, y-height/2, y-height/2, y-height/2 + corner};
        g.fillPolygon(xPoints, yPoints, 8);
    }

    /**
     * Rotates the module
     * @param dir Which direction to rotate in
     */
    public final void rotate(rotationDir dir) {
        switch (dir) {
            case ROT_CW:
                orientation = (orientation + 1) % 4;
                break;
            case ROT_CCW:
                orientation = (orientation - 1) % 4;
                break;
            case ROT_180:
                orientation = (orientation + 2) % 4;
                break;
        }
    }

    /**
     * Updates the object's transformation
     */
    public void updateXForm() {
        snapToGrid();

        toWorld = new AffineTransform();
        toWorld.translate(pos.x, pos.y);
        toWorld.rotate((Math.PI / 2) * orientation);

        toView = new AffineTransform(Main.ui.view.wToV);
        toView.concatenate(toWorld);

        // Update links
        for (Port p : ports) {
            if (p.link != null) p.link.updatePath();
        }
    }

    /**
     * Generates on-grid coords
     */
    public void snapToGrid() {
        pos.x = Math.round(pos.x / Main.sim.grid) * Main.sim.grid;
        pos.y = Math.round(pos.y / Main.sim.grid) * Main.sim.grid;
    }

    /**
     * Transforms a point from object to world-space
     * @param p Point
     * @return Transformed point
     */
    public Vec2 objToWorld(Vec2 p) {
        double[] pt = p.asArray();

        toWorld.transform(pt, 0, pt, 0, 1);

        return new Vec2(pt);
    }

    /**
     * Removes a module from the sim. DOES NOT affect its links.
     * Creates a new delete operation.
     */
    @Override
    public void delete() {
        Main.sim.removeEntity(this);
        Main.selection.remove(this);
        Main.opStack.pushOp(new DeleteOperation(this));
    }

    /**
     * Handles user interaction through parts
     * @param ix X coord in view space
     * @param iy Y coord in view space
     * @return Whether the input was handled
     */
    public boolean lbDown(int ix, int iy, boolean isShiftDown) {
        if (!enabled) return false;

        // Coords in object space
        double[] pt = {ix, iy};
        try {toView.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {e.printStackTrace();}

        int dx = (int)pt[0];
        int dy = (int)pt[1];

        synchronized (parts) {
            for (VisiblePart p : parts) {
                if (p.lbDown(dx, dy, isShiftDown)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles user interaction through parts
     * @param ix X coord in view space
     * @param iy Y coord in view space
     * @return Whether the input was handled
     */
    public boolean lbUp(int ix, int iy) {
        if (!enabled) return false;

        // Coords in object space
        double[] pt = {ix, iy};
        try {toView.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {e.printStackTrace();}

        int dx = (int)pt[0];
        int dy = (int)pt[1];

        synchronized (parts) {
            for (VisiblePart p : parts) {
                if (p.lbUp(dx, dy)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean within(double x, double y, double x2, double y2) {
        double[] rect = {x, y, x2, y2};

        // Get clicked point in object space
        try {toWorld.inverseTransform(rect, 0, rect, 0, 2);}
        catch (Exception e) {
            System.err.println("Non invertible transform");
        }

        x  = Math.min(rect[0], rect[2]);
        y  = Math.min(rect[1], rect[3]);
        x2 = Math.max(rect[0], rect[2]);
        y2 = Math.max(rect[1], rect[3]);

        return x < -w / 2 && x2 > w / 2 &&
                y < -h / 2 && y2 > h / 2;
    }

    @Override
    public boolean intersects(Vec2 pt) {
        double[] dpt = pt.asArray();

        // Get clicked point in object space
        try {toWorld.inverseTransform(dpt, 0, dpt, 0, 1);}
        catch (Exception e) {
            System.err.println("Non invertible transform");
        }

        double nx = dpt[0];
        double ny = dpt[1];

        return nx > -w / 2 && nx < w / 2 &&
                ny > -h / 2 && ny < h / 2;
    }

    @Override
    public int getType() {
        return PickableEntity.MODULE;
    }

    /**
     * Updates the module's outputs based on its inputs
     * (Needs override)
     */
    public abstract void propagate();

    /**
     * Propagates a bidirectional port's directionality.<br/>Note: this is recursive through the setMode() calls!
     * @param root Port to base directionality on
     */
    public void propagateDirectionality(BidirPort root) {
        for (Port p : bidirs) {
            if (p == root) continue;
            Port.Mode rootMode = root.getMode();

            Port.Mode newPMode, oppositeMode;
            if (p.side == root.side) {
                newPMode = rootMode;
                oppositeMode = Port.OppositeOf(rootMode);
            }
            else {
                newPMode = Port.OppositeOf(rootMode);
                oppositeMode = rootMode;
            }

            // Set this port's mode to match or oppose the root's mode based on which side it's on
            p.setMode(newPMode);

            // Propagate through any links
            if (p.link != null) {
                // Set the opposite port on the link to the opposite mode from us
                //  *the opposite of bidirectional is bidirectional!
                if (p.link.src == p) {
                    p.link.targ.setMode(oppositeMode);
                }
                else {
                    p.link.src.setMode(oppositeMode);
                }
            }
        }
    }

    /**
     * Run tests on the module
     * @return True if tests ran successfully
     */
    public boolean test() {return true;}

    /**
     * Initialize state with a loaded hash map structure (module-specific implementation)
     * Called by XMLReader and copy routines. Default behaviour is no-op.
     * @param data Structure containing state to load (module-defined elements)
     */
    public void dataIn(HashMap<String, String> data) {
        if (data.containsKey("label")) {
            label = data.get("label");
        }
        if (data.containsKey("label_size")) {
            String sizeStr = data.get("label_size");
            try {
                labelSize = Integer.parseInt(sizeStr);
            } catch (NumberFormatException e) {
                System.err.println("Warning: unable to parse label_size:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Fill a string-string hash map with module-specific data for retrieval with dataIn.
     * Called by XMLWriter and the copy routines. Default behaviour is to return null, indicating that no relevant
     * data is contained in the module.
     * @return A filled hash map structure, or null if no state is stored
     */
    public HashMap<String, String> dataOut() {
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("label", label);
        dataMap.put("label_size", Integer.toString(labelSize));
        return dataMap;
     }

    public enum AvailableModules {
        // Enum members should not be renamed!
        ADDSUB(new AddSub(), "Arithmetic Unit"),
        CLOCK(new Clock(), "Clock"),
        DEMUX(new Demux(), "Demultiplexor"),
        FANOUT(new Fanout(), "Fanout"),
        LOGIC(new Logic(), "Logic Unit"),
        MUX(new Mux(), "Multiplexor"),
        OR(new Or(), "OR"),
        RAM(new NRAM(true), "NRAM"), // note: the old "RAM" name is part of the file format and so can't be changed
        REGISTER(new Register(), "Register"),
        LEFT_SHIFT(new Shift(true), "Left-shift"),
        RIGHT_SHIFT(new Shift(false), "Right-shift"),
        SPLIT_MERGE(new SplitMerge(), "Splitter / Merger"),
        SWITCH(new SwitchInput(), "Switch Input"),
    	LEDMatrix(new LEDMatrix(), "16x16 LED matrix");

        /**
         * The module represented by this enum value, to use to instantiate and display in GUI.
         */
        private final BaseModule module;
        private final String name;

        AvailableModules(BaseModule mod, String name) {
            this.module = mod;
            this.name = name;
        }

        public BaseModule getSrcModule() {
            return module;
        }

        @Override
        public String toString() {
            return name;
        }

        public static AvailableModules fromModule(BaseModule mod) throws IllegalArgumentException {
            for (AvailableModules am : values()) {
                if (am.module.getClass().equals(mod.getClass())) {
                    return am;
                }
            }

            throw new IllegalArgumentException("Module of type " + mod.getClass() + " is not available!");
        }
    }

}
