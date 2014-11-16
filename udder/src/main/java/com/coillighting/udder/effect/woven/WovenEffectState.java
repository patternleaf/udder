package com.coillighting.udder.effect.woven;

import com.coillighting.udder.model.Pixel;

public class WovenEffectState {
    protected Pixel warpThreadColor;
    protected Pixel weftThreadColor;

    public WovenEffectState(Pixel warpThreadColor, Pixel weftThreadColor) {
        this.warpThreadColor = warpThreadColor;
        this.weftThreadColor = weftThreadColor;
    }

    public Pixel getWarpThreadColor() {
        return warpThreadColor;
    }

    public void setWarpThreadColor(Pixel warpThreadColor) {
        this.warpThreadColor = warpThreadColor;
    }

    public Pixel getWeftThreadColor() {
        return weftThreadColor;
    }

    public void setWeftThreadColor(Pixel weftThreadColor) {
        this.weftThreadColor = weftThreadColor;
    }
}
