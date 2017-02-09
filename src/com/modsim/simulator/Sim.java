package com.modsim.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JOptionPane;

import com.modsim.Main;
import com.modsim.modules.*;
import static com.modsim.modules.BaseModule.AvailableModules;
import com.modsim.modules.parts.Port;
import sun.awt.Mutex;

import com.modsim.util.BinData;
import com.modsim.util.CtrlPt;

public class Sim implements Runnable {

    private Thread thread;
    public final Mutex lock = new Mutex();

    private int lastLinkInd = 0;

    public static long delay = 2500000;
    public volatile boolean running = false;

    public String filePath = "";

    // Module list
    private final List<BaseModule> modules = new ArrayList<>();
    private final List<BaseModule> propModules = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private final List<PickableEntity> entities = new ArrayList<>();

    public double itrPerSec = 0;
    public int iterations = 0;

    // Deferred propagation mechanism
    private List<BaseModule> deferredPropagators = new ArrayList<>();
    private int deferring = 0;

    private Queue<QueueItem> propagationQueue;
    
    /**
     * Begin deferring propagation operations (preventing errors during large-scale operations)
     */
    public void beginDeferPropagations() {
        deferring++;
    }

    /**
     * Finish deferring propagation operations (carries out the deferred propagations)
     */
    public void endDeferPropagations() {
        deferring--;
        assert(deferring >= 0);

        if (deferring == 0) {
            for (BaseModule m : deferredPropagators) {
                propagate(m);
            }

            deferredPropagators.clear();
        }
    }

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
        // Reset the camera position
        Main.ui.view.camX = 0;
        Main.ui.view.camY = 0;

        synchronized (this) {
            modules.clear();
            links.clear();
            propModules.clear();
            entities.clear();

            Main.opStack.clearAll();
            filePath = "";
            Main.ui.updateTitle();
        }
		propagationQueue = new LinkedList<QueueItem>();
        Main.ui.view.flagStaticRedraw();
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
     * Thread safe link (& control points) addition
     */
    public void addLink(Link l) {
        synchronized (this) {
            clearErrors();
            links.add(l);

            for (CtrlPt c : l.path.getCtrlPts()) {
                addEntity(c);
            }
        }
    }

    /**
     * Yields a unique ID for a link
     */
    public int assignLinkID() {
        return lastLinkInd++;
    }

    /**
     * Thread safe link remove
     */
    public void removeLink(Link l) {
        synchronized (this) {
            links.remove(l);
        }
        l.src.link = null;
        l.targ.setVal(new BinData());
    }

    /**
     * Removes error flags from com.modsim.modules
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
        synchronized (lock) {
            // Don't run while we're deferring operations
            if (deferring != 0) return;

            //System.out.print("\nIteration " + iterations + " : ");
            iterations++;

            for (int i = 0; i < propModules.size(); i++) {
                BaseModule m = propModules.get(i);

                // Tick the clock(s)
                if (m.getModType().equals(AvailableModules.CLOCK)) {
                    ((Clock) m).tick();
                }

                // Begin propagation at the clocks AND switches
                propagate(m);
            }
        }

        // Request view update
        Main.ui.view.flagDynamicRedraw();
    }

    /**
     * Thread-local propagation
     * @param m Module to propagate on
     * @param visited ID-indexed array indicating whether we've already propagated over each module
     */
    private void doPropagate(BaseModule m, boolean[] visited) {
        if (deferring != 0) {
            deferredPropagators.add(m);
        }
        else {
            if (m == null) return;
            m.propagate();

            for (Port p : m.ports) {
                if (!p.canOutput()) {
                    p.updated = false;
                    continue;
                }
                if (p.wasUpdated() && p.link != null) {
                    // First make sure 'visited' array is big enough
                    int id = p.link.getLinkID();
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
                    boolean[] clone = visited.clone();
                    clone[id] = true;
                    propagationQueue.add(new QueueItem(p.link.targ.owner, clone));
                }
                p.updated = false;
            }
        }
    }

    /**
     * Propagates through a module
     * @param m Module to propagate
     */
    public void propagate(BaseModule m) {
        synchronized (lock) {
        	propagationQueue.add(new QueueItem(m, new boolean[1024]));
        	while(!propagationQueue.isEmpty()){
        		QueueItem it = propagationQueue.remove();
        		doPropagate(it.baseModule, it.visited);
        	}
        }
    }
    
    class QueueItem {
    	private BaseModule baseModule;
    	private boolean[] visited;
    	public QueueItem(BaseModule m, boolean[] v){
    		baseModule = m;
    		visited = v;
    	}
    }
    
}
