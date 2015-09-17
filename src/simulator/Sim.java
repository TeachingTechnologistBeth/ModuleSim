package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import modules.*;
import static modules.BaseModule.AvailableModules;
import modules.parts.Port;
import util.BinData;
import util.CtrlPt;

public class Sim implements Runnable {

    private Thread thread;

    private boolean[] visited = new boolean[1024];
    private int lastLinkInd = 0;

    public static long delay = 2500000;
    public volatile boolean running = false;

    public String filePath = "";

    // Module list
    private final List<BaseModule> modules = new ArrayList<BaseModule>();
    private final List<BaseModule> propModules = new ArrayList<BaseModule>();
    private final List<Link> links = new ArrayList<Link>();
    private final List<PickableEntity> entities = new ArrayList<PickableEntity>();

    public double itrPerSec = 0;
    public int iterations = 0;

    /**
     * Start the sim
     */
    public void start() {
        running = true;
        clearErrors();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stop the sim
     */
    public void stop() {
        clearErrors();
        running = false;
        itrPerSec = 0;
    }

    // Grid size
    public int grid = 25;

    // New simulation
    public void newSim() {
        synchronized (this) {
            modules.clear();
            links.clear();
            propModules.clear();
            entities.clear();

            Main.ui.view.opStack.clearAll();
            filePath = "";
            Main.ui.updateTitle();
        }
    }

    /**
     * Module access (MUST be contained in
     * synchronized (Main.sim) block)
     */
    public List<BaseModule> getModules() {
        return modules;
    }

    /**
     * Entity access (MUST be contained in
     * synchronized (Main.sim) block)
     */
    public List<PickableEntity> getEntities() {
        return entities;
    }

    /**
     * Link access (MUST be contained in
     * synchronized (Main.sim) block)
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * Thread safe entity add
     */
    public void addEntity(PickableEntity ent) {
        synchronized (this) {
            clearErrors();

            if (ent.getType() == PickableEntity.MODULE) {
                BaseModule m = (BaseModule) ent;
                modules.add(m);
                if (m.getModType() == AvailableModules.CLOCK) {
                    propModules.add(m);
                }
            }
            entities.add(ent);
        }
    }

    /**
     * Thread safe entity removal. Removes module links.
     */
    public void removeEntity(PickableEntity ent) {
        synchronized (this) {
            entities.remove(ent);

            // Module-specific cleanup also removes leftover links
            if (ent.getType() == PickableEntity.MODULE) {
                BaseModule module = (BaseModule) ent;
                modules.remove(ent);
                propModules.remove(ent);

                for (Port p : module.ports) {
                    if (p.link != null) {
                        p.link.delete();
                    }
                }
            }
        }
    }

    /**
     * Thread-safe, sensible multi-entity removal.
     * Link state within the removed group is preserved, and the affected entities are removed from the
     * simulation. This allows correct restoration at a later point, e.g. during a redo op.
     *
     * Note: Implicitly calls removeLink() on all associated links.
     */
    public void removeEntities(Collection<PickableEntity> ents) {
        synchronized (this) {
            entities.removeAll(ents);

            for (PickableEntity e : ents) {
                if (e.getType() == PickableEntity.MODULE) {
                    BaseModule module = (BaseModule) e;
                    modules.remove(e);
                    propModules.remove(e);

                    for (Port p : module.ports) {
                        if (p.link != null) {
                            Link l = p.link;
                            removeLink(l);

                            // Link to/from another removed entity
                            if (ents.contains(l.src.owner) && ents.contains(l.targ.owner)) {
                                // do nothing..?
                            }
                            // Link TO an outside entity
                            else if (ents.contains(l.src.owner)) {
                                // Break the link at the other end and propagate the change
                                l.targ.link = null;
                                l.targ.setVal(new BinData());
                                propagate(l.targ.owner);
                            }
                            // Link FROM an outside entity
                            else {
                                // Break the link at the other end
                                l.src.link = null;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Thread safe link (& control points) addition
     */
    public void addLink(Link l) {
        synchronized (this) {
            clearErrors();
            links.add(l);
            l.linkInd = lastLinkInd;
            lastLinkInd++;

            for (CtrlPt c : l.curve.getCtrlPts()) {
                addEntity(c);
            }
        }
    }

    /**
     * Thread safe link remove
     */
    public void removeLink(Link l) {
        synchronized (this) {
            links.remove(l);
        }
    }

    /**
     * Removes error flags from modules
     */
    public void clearErrors() {
        for (BaseModule m : modules) {
            m.error = false;
        }
    }

    public void run() {
        int iterations = 0;
        long start = System.currentTimeMillis();

        // Runs the sim constantly
        while(running) {
            // Iterate
            step();

            // Calculate speed
            iterations++;
            long now = System.currentTimeMillis();
            long delta = now - start;

            if (delta > 1000) {
                itrPerSec = iterations;
                iterations = 0;
                start = now;
            }

            // Speed control
            if (delay != 0) {
                nanoWait(delay);
            }
        }
    }

    /**
     * Nanosecond(ish)-accurate wait
     */
    private void nanoWait(long interval) {
        int ms = (int) (interval / 100000);
        try {Thread.sleep(ms);}
        catch (Exception e) {
            System.err.println("Warning: thread sleep exception!");
        }

        interval = interval % 100000;

        long start = System.nanoTime();
        long end;

        do {
            end = System.nanoTime();
        } while (start + interval >= end);
    }

    /**
     * Recursive simulation
     */
    public void step() {
        //System.out.print("\nIteration " + iterations + " : ");
        iterations++;

        // New ID array
        visited = new boolean[Main.sim.getEntities().size()];

        for (int i = 0; i < propModules.size(); i++) {
            BaseModule m = propModules.get(i);

            // Tick the clock(s)
            if (m.getModType().equals(AvailableModules.CLOCK)) {
                ( (Clock) m ).tick();
            }

            // Begin propagation at the clocks AND switches
            propagate(m);
        }
    }

    /**
     * Propagates through a module
     * @param m Module to propagate
     */
    public void propagate(BaseModule m) {
        if (m == null) return;
        m.propagate();

        for (Port p : m.ports) {
            if (!p.canOutput()) {
                p.updated = false;
                continue;
            }
            if (p.wasUpdated() && p.link != null) {
                // First make sure 'visited' array is big enough
                int id = p.link.linkInd;
                if (id >= visited.length) {
                    visited = Arrays.copyOf(visited, id * 2 + 1);
                }

                // Check if we've visited this link before
                if (visited[id]) {
                    p.owner.error = true;
                    running = false;
                    JOptionPane.showMessageDialog(null, "Runtime loop detected! Halting simulation. Did you forget a register?");
                    return;
                }

                // Recursively propagate
                if (p.link.targ == null) {
                    System.out.println("Warning: Null propagation target");
                    return;
                }
                p.link.targ.setVal(p.getVal());

                // Add link to visited - remove after propagation
                visited[id] = true;
                propagate(p.link.targ.owner);
                visited[id] = false;
            }
            p.updated = false;
        }
    }
}
