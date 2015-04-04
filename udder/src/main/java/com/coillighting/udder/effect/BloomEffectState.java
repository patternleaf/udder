package com.coillighting.udder.effect;

import com.coillighting.udder.model.Pixel;

/** Convey public parameters to and from BloomEffect instances.
 *  This class serves as a JSON mapping target for Boon.
 */
public class BloomEffectState {

    /** Tile these colors across the scene. 9 colors max. Ignore extras. */
    protected Pixel[] palette = null;

    /** Reflect the effect down the middle. */
    protected Boolean enableBilateralSym = null;

    /** Reflect the reflection. (Do nothing if enableBilateralSym is false.)
     *  Traditional for Blooming Leaves.
     */
    protected Boolean enableNestedBilateralSym = null;


    /** Vary the pattern in the X dimension. */
    protected Boolean enableX = null;

    /** Vary the pattern in the Y dimension. */
    protected Boolean enableY = null;

    public BloomEffectState(Pixel[] palette,
                            Boolean enableBilateralSym,
                            Boolean enableNestedBilateralSym,
                            Boolean enableX,
                            Boolean enableY)
    {
        this.palette = palette;
        this.enableBilateralSym = enableBilateralSym;
        this.enableNestedBilateralSym = enableNestedBilateralSym;
        this.enableX = enableX;
        this.enableY = enableY;
    }

    public Pixel[] getPalette() {
        return this.palette;
    }

    public void setPalette(Pixel[] palette) {
        this.palette = palette;
    }


    public Boolean getEnableBilateralSym() {
        return enableBilateralSym;
    }

    public void setEnableBilateralSym(Boolean enableBilateralSym) {
        this.enableBilateralSym = enableBilateralSym;
    }


    public Boolean getEnableNestedBilateralSym() {
        return enableNestedBilateralSym;
    }

    public void setEnableNestedBilateralSym(Boolean enableNestedBilateralSym) {
        this.enableNestedBilateralSym = enableNestedBilateralSym;
    }

    public Boolean getEnableX() {
        return enableX;
    }

    public void setEnableX(Boolean enableX) {
        this.enableX = enableX;
    }

    public Boolean getEnableY() {
        return enableY;
    }

    public void setEnableY(Boolean enableY) {
        this.enableY = enableY;
    }
}
