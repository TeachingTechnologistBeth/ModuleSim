package com.modsim.operations;

/**
 * Created by Ali on 17/08/2015.
 *
 * Manages the undo/redo stack as a fixed-size ring buffer
 */
public class OperationStack {
    private BaseOperation[] stack = new BaseOperation[MAX_HISTORY + 1];
    private int futureHead = 0, head = 0, tail = 0, size = 0;

    private CompoundOperation compoundOp;
    private int compound_stackSize = 0;

    public final static int MAX_HISTORY = 500;
    private boolean suppressOperations = false;

    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public void resetModified() {
        modified = false;
    }

    /**
     * Retrieves the length of the history (the number of potential undo ops remaining)
     * @return Number of operations in stack before head (excluding future redo queue)
     */
    public int getLength() {
        int count = 0;
        int pointer = tail;
        while (pointer != head) {
            count++;
            pointer++;
        }

        return count;
    }

    /**
     * Reverses a previously completed operation.
     */
    public void undo() {
        if (compoundOp != null) {
            // Whoops...
            System.err.println("Warning: cancelling compound op (user attempted to undo)");
            cancelCompoundOp();
            return;
        }

        // If head == tail we're out of undo-s
        if (head != tail) {
            // We undo @head-1
            head = (head - 1) % (MAX_HISTORY + 1);
            if (head < 0) head += (MAX_HISTORY + 1); // stupid java

            // Do the undo, suppressing additional operations
            suppressOperations = true;
            stack[head].undo();
            suppressOperations = false;

            // don't decrease size as we're still storing the future redo queue
        }
    }

    /**
     * Repeats a previously reversed operation.
     */
    public void redo() {
        if (compoundOp != null) {
            // Whoops...
            System.err.println("Warning: cancelling compound op (user attempted to redo)");
            cancelCompoundOp();
            return;
        }

        if (head != futureHead) {
            // We redo @head (where head '<' futureHead)
            suppressOperations = true;
            stack[head].redo();
            suppressOperations = false;

            head = (head+1) % (MAX_HISTORY + 1);
        }
    }

    /**
     * Adds a new operation to the undo stack; implicitly clears the possible redo-s.
     * If a compound operation is being generated, the operation is included within the compound operation.
     * @param op Operation to add
     */
    public void pushOp(BaseOperation op) {
        if (suppressOperations) return;

        if (compoundOp != null) {
            compoundOp.pushOp(op);
        }
        else {
            // Clear the 'future' redo queue
            // Can't use head < futureHead due to wrap-around
            while (head != futureHead) {
                stack[futureHead] = null;
                futureHead = (futureHead - 1) % (MAX_HISTORY + 1);
                if (futureHead < 0) futureHead += (MAX_HISTORY + 1);
                size--;
            }

            // Make room if there is none
            if (size == (MAX_HISTORY + 1) - 1) {
                stack[tail] = null;
                tail = (tail + 1) % (MAX_HISTORY + 1);
                size--;
            }

            // Store at the current head position (starting at 0) then increment
            // - futureHead always points to the next free slot (after all future redos)
            // - head points to the next *insertion point* and/or the next redo
            stack[head] = op;
            head = (head + 1) % (MAX_HISTORY + 1);
            futureHead = head;
            size++;

            // Mark as modified
            modified = true;
        }
    }

    /**
     * Clears the undo stack.
     */
    public void clearAll() {
        compoundOp = null;
        suppressOperations = false;

        for (int i = 0; i < (MAX_HISTORY + 1); i++) {
            stack[i] = null;
        }

        head = futureHead = tail = size = 0;
        modified = false;
    }

    /**
     * Begins a compound operation composed of multiple smaller operations. Must be matched to an endCompoundOp()
     * call. Throws UnsupportedOperationException if called while a compound operation is already in progress.
     */
    public void beginCompoundOp() {
        if (compoundOp != null) {
            compound_stackSize++;
        }
        else {
            compoundOp = new CompoundOperation();
            compound_stackSize = 1;
        }
    }

    /**
     * Completes a compound operation, adding it to the stack. Must be matched to a beginCompoundOp() call,
     * otherwise an UnsupportedOperationException will be thrown.
     */
    public void endCompoundOp() {
        if (compoundOp == null && compound_stackSize == 0) {
            throw new UnsupportedOperationException("Not in a compound operation");
        }
        else if (compoundOp == null) {
            // The operation has been cancelled (stack size > 0)
            compound_stackSize--;
        }
        else {
            compound_stackSize--;
            assert(compound_stackSize >= 0);
            if (compound_stackSize == 0) {
                if (compoundOp.getLength() > 0) {
                    // Do the pointer-shuffle ?( ?_?)?
                    CompoundOperation temp = compoundOp;
                    compoundOp = null;
                    pushOp(temp);
                }
                else {
                    // Don't do anything with empty compound operations
                    compoundOp = null;
                }
            }
        }
    }

    /**
     * Cancels a compound operation, undoing any sub-operations already submitted. Must be matched to a
     * beginCompoundOp() call.
     */
    public void cancelCompoundOp() {
        if (compoundOp == null) throw new UnsupportedOperationException("Not in a compound operation");
        else {
            try {
                suppressOperations = true;
                compoundOp.undo();
                suppressOperations = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

            compoundOp = null;
            compound_stackSize--;
        }
    }
}
