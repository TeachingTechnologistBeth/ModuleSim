package com.modsim.operations;

import com.modsim.modules.BaseModule;

/**
 * Created by awick on 21/09/2015.
 *
 * Undo/redo for label string changes
 */
public class LabelOperation extends BaseOperation {

    private BaseModule targetModule = null;

    public LabelOperation(BaseModule module, String oldLabel, String newLabel) {
        oldStr = oldLabel;
        newStr = newLabel;
        targetModule = module;
    }
    private String oldStr, newStr;

    @Override
    public void undo() {
        targetModule.label = oldStr;
    }

    @Override
    public void redo() {
        targetModule.label = newStr;
    }

}
