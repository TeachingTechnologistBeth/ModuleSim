package com.modsim.tools;

import java.util.ArrayList;
import java.util.List;

import com.modsim.gui.view.ViewUtil;
import com.modsim.Main;
import com.modsim.operations.CreateOperation;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.ModuleClipboard;
import com.modsim.util.Vec2;
import com.modsim.modules.BaseModule;

public class PlaceTool extends BaseTool {

    private List<PickableEntity> entities = new ArrayList<PickableEntity>();
    private Vec2 start = null;

    /**
     * Creates a placement tool for the specified module
     * @param e
     */
    public PlaceTool(PickableEntity e) {
        Main.opStack.beginCompoundOp();

        entities.add(e);
        Main.sim.addEntity(e); // add module to sim
        Main.opStack.pushOp(new CreateOperation(e));

        Vec2 p = new Vec2(-200, -200);
        p = ViewUtil.screenToWorld(p, false);
        e.move(p);
    }

    /**
     * Acts as a 'pasteInto' tool for the given clipboard
     * 
     * @param clipboard Clipboard containing com.modsim.modules to 'pasteInto'
     * @throws Exception
     */
    public PlaceTool(ModuleClipboard clipboard) throws Exception {
        Main.opStack.beginCompoundOp();
        entities = clipboard.paste();

        for (PickableEntity e : entities) {
            e.tempPos.set(e.pos);
        }
    }

    @Override
    public BaseTool mouseMove(int x, int y) {
        Vec2 cur = ViewUtil.screenToWorld(new Vec2(x, y), false);

        if (start == null) {
            start = new Vec2(cur);

            Vec2 delta = new Vec2(start);
            delta.sub(entities.get(0).tempPos);

            entities.get(0).tempPos.set(start);
            for (int i = 1; i < entities.size(); i++) {
                entities.get(i).tempPos.add(delta);
            }
        }
        cur.sub(start);

        for (PickableEntity e : entities) {
            e.moveRelative(cur);
        }

        // Update view
        Main.ui.view.flagStaticRedraw();
        return this;
    }

    @Override
    public BaseTool lbDown(int x, int y, boolean isShiftDown) {
        // Make sure the positions are up to date
        mouseMove(x, y);

        if (BaseTool.SHIFT && entities.size() == 1) {
            PickableEntity e = entities.get(0);
            e.enabled = true;

            if (e.getClass().getSuperclass() == BaseModule.class) {
                Main.opStack.endCompoundOp();
                return new PlaceTool(e.createNew()); // repeat op
            }
        }
        else
        {
            // Select whatever we've just placed
            Main.selection.clear();
            for (PickableEntity e : entities) {
                e.enabled = true;
                Main.selection.add(e);
            }
            Main.ui.compPane.selected = null;
            Main.ui.compPane.repaint();
        }

        // Complete the overall operation
        Main.opStack.endCompoundOp();
        return null;
    }

    @Override
    public void cancel() {
        entities.clear();
        Main.ui.compPane.selected = null;
        Main.ui.compPane.repaint();

        // Cancelling automagically undoes our changes
        Main.opStack.cancelCompoundOp();
    }

}
