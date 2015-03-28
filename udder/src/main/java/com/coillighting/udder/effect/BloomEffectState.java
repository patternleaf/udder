package com.coillighting.udder.effect;

/** Convey public parameters to and from BloomEffect instances.
 *  This class serves as a JSON mapping target for Boon.
 */
public class BloomEffectState {

    protected int temp = 0;

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
