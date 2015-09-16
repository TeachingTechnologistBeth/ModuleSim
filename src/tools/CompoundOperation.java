package tools;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Ali on 17/08/2015.
 */
public class CompoundOperation extends BaseOperation {
    private ArrayList<BaseOperation> ops = new ArrayList<BaseOperation>();

    public void pushOp(BaseOperation op) {
        ops.add(op);
    }

    public int getLength() {
        return ops.size();
    }

    @Override
    public void undo() {
        // Reverse iterate
        ListIterator<BaseOperation> li = ops.listIterator(ops.size());
        while (li.hasPrevious()) {
            li.previous().undo();
        }
    }

    @Override
    public void redo() {
        // Forward iterate
        for (BaseOperation op : ops) {
            op.redo();
        }
    }
}
