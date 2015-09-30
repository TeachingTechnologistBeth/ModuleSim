package com.modsim.operations;

import com.modsim.modules.BaseModule;

/**
 * Created by Ali on 31/08/2015.
 */
public class RotateOperation extends BaseOperation {
    private BaseModule.rotationDir rotDir;

    public RotateOperation(BaseModule e, BaseModule.rotationDir dir) {
        entity = e;
        rotDir = dir;
    }

    @Override
    public void undo() {
        BaseModule m = (BaseModule) entity;
        switch (rotDir) {
            case ROT_CW:
                m.rotate(BaseModule.rotationDir.ROT_CCW);
                break;
            case ROT_CCW:
                m.rotate(BaseModule.rotationDir.ROT_CW);
                break;
            case ROT_180:
                m.rotate(BaseModule.rotationDir.ROT_180);
                break;
        }
    }

    @Override
    public void redo() {
        BaseModule m = (BaseModule) entity;
        m.rotate(rotDir);
    }

}
