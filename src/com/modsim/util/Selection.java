package com.modsim.util;

import com.modsim.modules.BaseModule;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awick on 18/09/2015.
 */
public class Selection {

    protected ArrayList<PickableEntity> internalSelection = new ArrayList<>();
    protected boolean isMain = false;

    public Selection(boolean isMain) {
        this.isMain = isMain;
    }

    public Selection(Selection other) {
        internalSelection = new ArrayList<>(other.internalSelection);
    }

    // Selection management


    public void add(PickableEntity entity) {
        internalSelection.remove(entity);
        internalSelection.add(entity);
        if (isMain) entity.selected = true;

        if (entity.getType() == PickableEntity.MODULE) {
            ((BaseModule)entity).error = false;
        }
    }

    public void addAll(List<PickableEntity> entities) {
        for (PickableEntity e : entities) {
            internalSelection.remove(e);
            internalSelection.add(e);
            if (isMain) e.selected = true;
        }
    }

    public void addAll(Selection other) {
        addAll(other.internalSelection);
    }

    public void set(List<PickableEntity> entities) {
        clear();
        addAll(entities);
    }

    public void set(PickableEntity entity) {
        clear();
        if (entity != null) add(entity);
    }

    public void set(Selection other) {
        clear();
        addAll(other.internalSelection);
    }

    public void toggle(PickableEntity e) {
        if (internalSelection.contains(e)) {
            internalSelection.remove(e);
            e.selected = false;
        }
        else {
            internalSelection.add(e);
            e.selected = true;
        }
    }

    public void remove(BaseModule m) {
        internalSelection.remove(m);
        m.selected = false;
    }

    public void clear() {
        for (PickableEntity e : internalSelection) {
            e.selected = false;
        }

        internalSelection.clear();
    }

    public void deleteAll() {
        Main.opStack.beginCompoundOp();
        ArrayList<PickableEntity> forDeletion = new ArrayList<>(internalSelection);
        for (PickableEntity e : forDeletion) {
            e.delete();
        }
        clear();
        Main.opStack.endCompoundOp();
        //redraw
        Main.ui.view.flagStaticRedraw();
    }

    public boolean isEmpty() {
        return internalSelection.isEmpty();
    }

    public void showContextMenu(int x, int y) {
        Main.ui.popup.showEntityMenu(internalSelection, x, y);
    }

    public ArrayList<PickableEntity> getEntities() {
        return new ArrayList<>(internalSelection);
    }

}
