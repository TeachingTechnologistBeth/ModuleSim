package com.modsim.operations;

import com.modsim.simulator.PickableEntity;
import com.modsim.util.Vec2;

/**
 * Created by Ali on 30/08/2015.
 */
public class MoveOperation extends BaseOperation {
    private Vec2 delta = new Vec2();

    public MoveOperation(PickableEntity e, Vec2 d) {
        entity = e;
        delta.set(d);
    }

    @Override
    public void undo() {
        Vec2 pos = new Vec2(entity.pos);
        pos.sub(delta);
        entity.pos.set(pos);
    }

    @Override
    public void redo() {
        Vec2 pos = new Vec2(entity.pos);
        pos.add(delta);
        entity.pos.set(pos);
    }

}
