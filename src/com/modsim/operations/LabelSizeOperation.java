package com.modsim.operations;

import com.modsim.modules.BaseModule;

/**
 * Created by Ali on 24/09/2015.
 *
 * Undo/redo for label size changes
 */
public class LabelSizeOperation extends BaseOperation {

    private int oldSize, newSize;
    private BaseModule module;

    public LabelSizeOperation(BaseModule module, int oldSize, int newSize) {
        this.module = module;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    @Override
    public void undo() {
        module.labelSize = oldSize;
    }

    @Override
    public void redo() {
        module.labelSize = newSize;
    }

}
