package com.modsim.modules;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.modsim.modules.parts.Port;
import com.modsim.Main;
import com.modsim.res.Colors;
import com.modsim.operations.DeleteOperation;
import com.modsim.util.Path;
import com.modsim.util.BezierPath;
import com.modsim.util.StraightPath;
import com.modsim.util.BinData;

/**
 * Link between two ports
 * @author aw12700
 *
 */
public class Link {

    public Port src;
    public Port targ;
    public Path path;

    public boolean highlight = false;

    private int linkInd = -1;

    /**
     * Gets a unique ID for the link
     * @return
     */
    public int getLinkID() {
        if (linkInd < 0) {
            linkInd = Main.sim.assignLinkID();
        }

        return linkInd;
    }

    /**
     * Creates a new link between two ports, which may be reversed depending on type of source and target.
     * @param source The first clicked port
     * @param target The second clicked port
     * @param path A bezier path to display for the link
     * @return New link, or null if link was invalid
     */
    public static Link createLink(Port source, Port target, Path path) {
        // Check error conditions first

    	if (source == null || target == null) {
            // there's no message dialog here as it's not the user's fault - this generally happens when opening
            // corrupted files
    	    System.err.println("No connect: Port(s) do not exist");
    	    return null;
    	}

        // No self-links
    	else if (source == target) {
    	    JOptionPane.showMessageDialog(Main.ui.frame,
                    "Cannot link port to itself",
                    "Invalid Link",
                    JOptionPane.WARNING_MESSAGE);
    	    return null;
    	}
        else if (source.owner == target.owner) {
            JOptionPane.showMessageDialog(Main.ui.frame,
                    "Cannot link module to itself",
                    "Invalid Link",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // If two directional ports are either both outputs or both inputs, they cannot be linked
        else if (source.canOutput() == target.canOutput() && source.hasDirection() && target.hasDirection()) {
            JOptionPane.showMessageDialog(Main.ui.frame,
                    "Cannot link same port types together",
                    "Invalid Link",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else {
            // Start a compound operation (likely nested) so we can abort cleanly
            Main.opStack.beginCompoundOp();

            // Cleanup old links
            if (source.link != null) {
                source.link.delete();
            }
            if (target.link != null) {
                target.link.delete();
            }

            Link newLink = new Link();
            source.link = newLink;
            target.link = newLink;

            // Pick direction of link

            // If we're between two bi-dirs, the user's word is law
            if (!source.hasDirection() && !target.hasDirection()) {
                // (attempt to) create link in direction chosen
                newLink.src = source;
                newLink.targ = target;
                newLink.path = path;

                source.setMode(Port.Mode.MODE_OUTPUT);
                target.setMode(Port.Mode.MODE_INPUT);
            }
            else if (source.canOutput() && target.canInput()) {
                newLink.src = source;
                newLink.targ = target;
                newLink.path = path;

                if (!source.hasDirection()) {
                    source.setMode(Port.Mode.MODE_OUTPUT);
                }

                if (!target.hasDirection()) {
                    target.setMode(Port.Mode.MODE_INPUT);
                }
            }
            else if (source.canInput() && target.canOutput()) {
                newLink.src = target;
                newLink.targ = source;
                path.reverse();
                newLink.path = path;

                if (!source.hasDirection()) {
                    source.setMode(Port.Mode.MODE_INPUT);
                }

                if (!target.hasDirection()) {
                    target.setMode(Port.Mode.MODE_OUTPUT);
                }
            }
            else {
                JOptionPane.showMessageDialog(Main.ui.frame,
                        (new Throwable()).getStackTrace(),
                        "Unknown error during link creation",
                        JOptionPane.ERROR_MESSAGE);
                Main.opStack.cancelCompoundOp();
                return null;
            }

            // Check loops
            List<BaseModule> modules = new ArrayList<>();
            if (newLink.checkLoops(newLink, modules)) {
                JOptionPane.showMessageDialog(Main.ui.frame,
                        "Link would create a loop. Have you forgotten a register?",
                        "Invalid link",
                        JOptionPane.WARNING_MESSAGE);
                System.err.println("No connect: Loop detected");
                source.link = null;
                target.link = null;

                for (BaseModule m : modules) {
                    m.error = true;
                }

                Main.opStack.cancelCompoundOp();
                return null;
            }

            // Changes are done
            Main.opStack.endCompoundOp();

            newLink.targ.setVal(newLink.src.getVal());

            return newLink;
        }
    }

    /**
     * Recursively check for loops in the design
     * @param check Link to check for
     * @param modules List of com.modsim.modules checked - used for error display
     * @return Whether the check link was found
     */
    private boolean checkLoops(Link check, List<BaseModule> modules) {
        // Registers & NRAM *should* terminate loops
        BaseModule.AvailableModules type = targ.owner.getModType();
        if (type == BaseModule.AvailableModules.RAM || type == BaseModule.AvailableModules.REGISTER) return false;

        // Follow every affected outbound link
        boolean result = false;
        for (Port p : targ.owner.getAffected(targ)) {
            if (p.canOutput() && p.link != null) {
                if (p.link == check) {
                    modules.add(targ.owner);
                    return true; // Loop detected
                }
                else {
                    result |= p.link.checkLoops(check, modules);
                    if (result) modules.add(targ.owner);
                }
            }
        }

        return result;
    }

    /**
     * Draw the link - colour is picked based on the port types
     * @param g Graphics context to draw with
     */
    public void draw(Graphics2D g) {
        if (highlight) {
            g.setColor(Colors.selectedLink);
        }
        else {
            g.setColor(Colors.blendedLinks[src.type][targ.type]);
        }

        g.setStroke(new BasicStroke(2));
        path.draw(g);
    }

    /**
     * Updates the Bezier path for display
     */
    public void updatePath() {
        // Generate the path
        path.setStart(src);
        path.setEnd(targ);
        path.calcCurves();
    }

    /**
     * Removes the link, along with references to it at the remaining ports.
     * The change causes a simulation propagation from the target module.
     * This method creates a new deletion operation.
     */
    public void delete() {
        src.link = null;
        targ.link = null;

        // Propagate change
        targ.setVal(new BinData());
        Main.sim.propagate(targ.owner);

        // Propagate (non-)directionality if applicable
        src.setMode(Port.Mode.MODE_BIDIR);
        targ.setMode(Port.Mode.MODE_BIDIR);

        // Remove from listings
        Main.sim.removeLink(this);

        // Store operation
        Main.opStack.pushOp(new DeleteOperation(this));
    }

}
