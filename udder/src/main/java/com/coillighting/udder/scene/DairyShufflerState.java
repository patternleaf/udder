package com.coillighting.udder.scene;

public class DairyShufflerState {

    protected boolean enabled;
    protected long cueDurationMillis;

    public DairyShufflerState(boolean enabled, long cueDurationMillis) {
        this.enabled = enabled;
        this.cueDurationMillis = cueDurationMillis;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCueDurationMillis() {
        return cueDurationMillis;
    }

    public void setCueDurationMillis(long cueDurationMillis) {
        this.cueDurationMillis = cueDurationMillis;
    }
}
