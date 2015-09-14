package tools;

import modules.Link;
import simulator.PickableEntity;

import java.util.Collection;

/**
 * Created by Ali on 16/08/2015.
 */
public abstract class BaseOperation {
    protected PickableEntity entity;
    protected Link link;

    public abstract void undo();
    public abstract void redo();
}
