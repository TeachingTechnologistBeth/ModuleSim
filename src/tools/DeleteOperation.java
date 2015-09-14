package tools;

import modules.Link;
import simulator.Main;
import simulator.PickableEntity;
import util.BinData;
import util.CtrlPt;

/**
 * Created by Ali on 17/08/2015.
 * Granular (single object) deletion.
 */
public class DeleteOperation extends BaseOperation {
    public DeleteOperation(PickableEntity e) {
        entity = e;
    }
    public DeleteOperation(Link l) {
        link = l;
    }

    @Override
    public void undo() {
        if (entity != null) {
            if (entity.getType() == PickableEntity.CTRLPT) {
                CtrlPt c = (CtrlPt) entity;
                c.parent.ctrlPts.add(c.index, c);
                c.parent.calcCurves();
            }
            Main.sim.addEntity(entity);
        }
        if (link != null) {
            link.src.link = link;
            link.targ.link = link;
            Main.sim.addLink(link);

            // Propagate change
            link.targ.setVal(link.src.getVal());
            Main.sim.propagate(link.targ.owner);
        }
    }

    @Override
    public void redo() {
        if (entity != null) {
            if (entity.getType() == PickableEntity.CTRLPT) {
                CtrlPt c = (CtrlPt) entity;
                c.parent.ctrlPts.remove(c);
                c.parent.calcCurves();
            }
            Main.sim.removeEntity(entity);
        }
        if (link != null) {
            link.src.link = null;
            link.targ.link = null;
            Main.sim.removeLink(link);

            // Propagate change
            link.targ.setVal(new BinData());
            Main.sim.propagate(link.targ.owner);
        }
    }
}
