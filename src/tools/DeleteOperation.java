package tools;

import modules.Link;
import modules.parts.Port;
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
                c.parent.addPt(c.index, c);
                c.parent.calcCurves();
            }
            Main.sim.addEntity(entity);
        }
        if (link != null) {
            link = Link.createLink(link.src, link.targ, link.curve);
            Main.sim.addLink(link);
        }
    }

    @Override
    public void redo() {
        if (entity != null) {
            if (entity.getType() == PickableEntity.CTRLPT) {
                CtrlPt c = (CtrlPt) entity;
                c.parent.removePt(c);
                c.parent.calcCurves();
            }
            Main.sim.removeEntity(entity);
        }
        if (link != null) {
            link.delete();
        }
    }
}
