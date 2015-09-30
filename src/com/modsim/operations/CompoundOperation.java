package com.modsim.operations;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Ali on 17/08/2015.
 */
public class CompoundOperation extends BaseOperation {
    private ArrayList<BaseOperation> ops = new ArrayList<BaseOperation>();

    public void pushOp(BaseOperation op) {
        synchronized (this) {
            ops.add(op);
        }
    }

    public int getLength() {
        synchronized (this) {
            return ops.size();
        }
    }

    @Override
    public void undo() {
        synchronized (this) {
            // Reverse iterate
            ListIterator<BaseOperation> li = ops.listIterator(ops.size());
            while (li.hasPrevious()) {
                li.previous().undo();
            }
        }
    }

    @Override
    public void redo() {
        synchronized (this) {
            // Forward iterate
            for (BaseOperation op : ops) {
                op.redo();
            }
        }
    }
}
