package com.modsim.modules.parts;

public abstract class TogglePart extends VisiblePart {

	private volatile boolean enabled = false;

	public void setEnabled(boolean en) {
		enabled = en;
	}

	public void toggleEnabled() {
		enabled = !enabled;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void reset() {
	    enabled = false;
	}

	@Override
	public RefreshMode getRefreshMode() {
        return RefreshMode.Dynamic;
    }
}
