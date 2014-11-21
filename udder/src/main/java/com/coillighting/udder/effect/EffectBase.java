package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** Abstract base class for typical Effects. */
public abstract class EffectBase implements Effect {

    protected Pixel[] pixels = null;
    protected Device[] devices = null;

    /** Reinitialize the Pixel array to match the size of the new patch sheet. */
    public void patchDevices(Device[] devices) {
        if(devices.length > 0) {
            this.devices = devices.clone();
        } else {
            this.devices = null;
        }
        this.initPixels(this.devices.length);
    }

    protected void initPixels(int length) {
        if(length > 0) {
            this.pixels = new Pixel[length];
            for(int i=0; i<this.pixels.length; i++) {
                this.pixels[i] = new Pixel();
            }
        } else {
            this.pixels = null;
        }
    }

    public void animate(TimePoint timePoint) { }

    /** See borrowing contract on Effect. */
    public Pixel[] render() {
        return this.pixels;
    }

    /** levelChanged notifications are sent by Layer when a fader is turned up
     *  past 0%. Most effects don't care, but some will want to rewind when
     *  that happens.
     */
    public void levelChanged(double oldLevel, double newLevel) {}

}
