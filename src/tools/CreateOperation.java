package tools;

import modules.Link;
import simulator.Main;
import simulator.PickableEntity;
import util.BinData;

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
            link.src.link = null;
            link.targ.link = null;
            Main.sim.removeLink(link);

            // Propagate change
            link.targ.setVal(new BinData());
            Main.sim.propagate(link.targ.owner);
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
            link.targ.setVal(link.src.getVal());
            Main.sim.propagate(link.targ.owner);
        }
    }
}
