package com.modsim.operations;

import com.modsim.modules.Link;
import com.modsim.simulator.PickableEntity;

/**
 * Created by Ali on 16/08/2015.
 */
public abstract class BaseOperation {
    protected PickableEntity entity;
    protected Link link;

    public abstract void undo();
    public abstract void redo();
}
