package util;

import modules.BaseModule;
import modules.Link;
import modules.parts.Port;
import simulator.Main;
import simulator.PickableEntity;
import tools.CreateOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ali on 05/09/2015.
 */
public class ModuleClipboard {

    public List<BaseModule> copiedModules = new ArrayList<>();
    public List<Link> copiedLinks = new ArrayList<>();

    /**
     * Whether the clipboard has any items on it
     * @return True if the clipboard is isEmpty
     */
    public boolean isEmpty() {
        return copiedLinks.isEmpty() && copiedModules.isEmpty();
    }

    /**
     * Copies the given entities to the clipboard in their current state. Only links and control points BETWEEN copied
     * modules are stored. Caller should ensure no modifications are made to the original entities while this method
     * is executing.
     * @param entities The entities to copy
     */
    public void copy(List<PickableEntity> entities) {
        // Wipe internal storage
        copiedModules.clear();
        copiedLinks.clear();

        // Pick out the modules from the generic entities list
        List<BaseModule> copiedRefs = new ArrayList<BaseModule>();
        for (PickableEntity e : entities) {
            if (e.getType() == PickableEntity.MODULE) {
                copiedRefs.add((BaseModule) e);
            }
        }

        // Copy across to clipboard storage
        doCopy(copiedRefs, copiedModules, copiedLinks);
    }

    /**
     * Copies the stored modules into the simulation, generating the corresponding creation operations, and returns
     * all created entities as a list.
     * @return The newly created entities
     */
    public List<PickableEntity> paste() {
        List<BaseModule> modules = new ArrayList<BaseModule>();
        List<Link> links = new ArrayList<Link>();

        doCopy(copiedModules, modules, links);
        List<PickableEntity> output = new ArrayList<PickableEntity>(modules);

        // Add to the simulation
        for (BaseModule m : modules) {
            Main.sim.addEntity(m);
            Main.ui.view.opStack.pushOp(new CreateOperation(m));
        }
        for (Link l : links) {
            Main.sim.addLink(l);
            Main.ui.view.opStack.pushOp(new CreateOperation(l));

            // Need to return control points as well
            output.addAll(l.curve.ctrlPts);
        }

        return output;
    }

    /**
     * Internal method: copies modules with their complete properties and shared links/control points
     * @param src List of modules to copy
     * @param destModules (out) list of copied modules
     * @param destLinks (out) list of copied links
     */
    protected void doCopy(List<BaseModule> src, List<BaseModule> destModules, List<Link> destLinks) {
        assert destModules != null && destModules.isEmpty();
        assert destLinks != null && destLinks.isEmpty();

        for (BaseModule m : src) {
            destModules.add((BaseModule) m.createNew());
        }

        // Properly copy across each module's properties
        for (int i = 0; i < destModules.size(); i++) {
            BaseModule oldM = src.get(i);
            BaseModule m = destModules.get(i);

            m.pos.set(oldM.pos);
            m.orientation = oldM.orientation;

            // Data copy
            HashMap<String, String> moduleData = oldM.dataOut();
            if (moduleData != null) {
                m.dataIn(moduleData);
                m.propagate();
            }

            // Link creation between copied modules
            for (int j = 0; j < m.ports.size(); j++) {
                Port newPort = m.ports.get(j);
                Port oldPort = oldM.ports.get(j);

                // Check it's a link between two copied entities
                if (oldPort.link != null
                        && oldPort == oldPort.link.src
                        && src.contains(oldPort.link.src.owner)
                        && src.contains(oldPort.link.targ.owner)) {

                    int targetModuleInd = src.indexOf(oldPort.link.targ.owner);
                    int targetPortInd = oldPort.link.targ.owner.ports.indexOf(oldPort.link.targ);
                    Port targetPort = destModules.get(targetModuleInd).ports.get(targetPortInd);

                    newPort.link = null;
                    targetPort.link = null;
                    Link newLink = Link.createLink(newPort, targetPort, new BezierPath(oldPort.link.curve));

                    // Store the new link
                    if (newLink != null) {
                        assert newLink.curve != oldPort.link.curve;

                        newPort.link = newLink;
                        destLinks.add(newLink);
                    }
                }
            }
        }
    }
}
