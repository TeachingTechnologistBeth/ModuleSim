package tools;

import modules.Link;
import modules.parts.Port;
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
            link.delete();
        }
    }

    @Override
    public void redo() {
        if (entity != null) Main.sim.addEntity(entity);
        if (link != null) {
            link = Link.createLink(link.src, link.targ, link.curve);
            Main.sim.addLink(link);
        }
    }
}
