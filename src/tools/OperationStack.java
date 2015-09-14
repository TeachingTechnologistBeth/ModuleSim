package tools;

/**
 * Created by Ali on 17/08/2015.
 */
public class OperationStack {
    private BaseOperation[] stack = new BaseOperation[MAX_HISTORY];
    private int futureHead = 0, head = 0, tail = 0, size = 0;

    private CompoundOperation compoundOp;

    public static int MAX_HISTORY = 500;

    /**
     * Reverses a previously completed operation.
     */
    public void undo() {
        if (compoundOp != null) throw new UnsupportedOperationException("Can't undo during a compound operation");

        // If head == tail we're empty
        if (head != tail) {
            // We undo @head-1
            head = (head-1) % MAX_HISTORY;
            stack[head].undo();
            // don't decrease size as we're still storing the future redo queue
        }
        else {
            System.out.println("Nothing to undo");
        }
    }

    /**
     * Repeats a previously reversed operation.
     */
    public void redo() {
        if (compoundOp != null) throw new UnsupportedOperationException("Can't redo during a compound operation");

        if (head != futureHead) {
            // We redo @head (where head '<' futureHead)
            stack[head].redo();
            head = (head+1) % MAX_HISTORY;
        }
        else {
            System.out.println("Nothing to redo");
        }
    }

    /**
     * Adds a new operation to the undo stack; implicitly clears the possible redo-s.
     * If a compound operation is being generated, the operation is included within the compound operation.
     * @param op Operation to add
     */
    public void pushOp(BaseOperation op) {
        if (compoundOp != null) {
            compoundOp.pushOp(op);
        }
        else {
            // Clear the 'future' redo queue
            // Can't use head < futureHead due to wrap-around
            while (head != futureHead) {
                stack[futureHead] = null;
                futureHead = (futureHead - 1) % MAX_HISTORY;
                size--;
            }

            // Make room if there is none
            if (size == MAX_HISTORY) {
                stack[tail] = null;
                tail = (tail + 1) % MAX_HISTORY;
                size--;
            }

            // Store at the current head position (starting at 0) then increment
            // - futureHead always points to the next free slot (after all future redos)
            // - head points to the next *insertion point* and/or the next redo
            stack[head] = op;
            head = (head + 1) % MAX_HISTORY;
            futureHead = head;
            size++;
        }
    }

    /**
     * Clears the undo stack.
     */
    public void clearAll() {
        compoundOp = null;

        for (int i = 0; i < MAX_HISTORY; i++) {
            stack[i] = null;
        }

        head = futureHead = tail = size = 0;
    }

    /**
     * Begins a compound operation composed of multiple smaller operations. Must be matched to an endCompoundOp()
     * call. Throws UnsupportedOperationException if called while a compound operation is already in progress.
     */
    public void beginCompoundOp() {
        if (compoundOp != null) throw new UnsupportedOperationException("Already in a compound operation");
        else {
            compoundOp = new CompoundOperation();
        }
    }

    /**
     * Completes a compound operation, adding it to the stack. Must be matched to a beginCompoundOp() call,
     * otherwise an UnsupportedOperationException will be thrown.
     */
    public void endCompoundOp() {
        if (compoundOp == null) throw new UnsupportedOperationException("Not in a compound operation");
        else {
            // Do the pointer-shuffle ?( ?_?)?
            CompoundOperation temp = compoundOp;
            compoundOp = null;
            pushOp(temp);
        }
    }

    /**
     * Cancels a compound operation, undoing any sub-operations already submitted. Must be matched to a
     * beginCompoundOp() call.
     */
    public void cancelCompoundOp() {
        if (compoundOp == null) throw new UnsupportedOperationException("Not in a compoud operation");
        else {
            compoundOp.undo();
            compoundOp = null;
        }
    }
}
