package com.modsim.operations;

import com.modsim.modules.Link;
import com.modsim.modules.parts.Port;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.CtrlPt;

/**
 * Created by Ali on 17/08/2015.
 * Granular (single object) deletion.
 */
public class DeleteOperation extends BaseOperation {
    public DeleteOperation(CtrlPt ctrlPt, int index) {
        if (index >= 0) {
            entity = ctrlPt;
            ctrlPtIndex = index;
        }
    }
    public DeleteOperation(PickableEntity e) {
        entity = e;
    }
    public DeleteOperation(Link l) {
        link = l;
    }

    private int ctrlPtIndex = -1;

    @Override
    public void undo() {
        if (entity != null) {
            if (entity.getType() == PickableEntity.CTRLPT) {
                assert(ctrlPtIndex >= 0);

                CtrlPt c = (CtrlPt) entity;
                c.parent.addPt(ctrlPtIndex, c);
                c.parent.calcCurves();
            }
            Main.sim.addEntity(entity);
        }
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
