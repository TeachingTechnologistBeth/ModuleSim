package com.modsim.tools;

import com.modsim.modules.Link;
import com.modsim.modules.parts.Port;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;

/**
 * Created by Ali on 31/08/2015.
 */
public class CreateOperation extends BaseOperation {

    public CreateOperation(PickableEntity e) { entity = e; }

    public CreateOperation(Link l) {
        link = l;
    }

    @Override
    public void undo() {
        if (entity != null) Main.sim.removeEntity(entity);
        if (link != null) {
            link.delete();
        }
    }

    @Override
    public void redo() {
        if (entity != null) Main.sim.addEntity(entity);
        if (link != null) {
            link.src.link = link;
            link.targ.link = link;
            Main.sim.addLink(link);

            // Propagate change
            link.src.setMode(Port.Mode.MODE_OUTPUT);
            link.targ.setMode(Port.Mode.MODE_INPUT);
            link.targ.setVal(link.src.getVal());
            Main.sim.propagate(link.targ.owner);
        }
    }
}
