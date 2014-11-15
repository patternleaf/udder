package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** Abstract base class for typical Effects. */
public abstract class EffectBase implements Effect {

    protected Pixel[] pixels = null;
    protected Device[] devices = null;

    /** Reinitialize the raster to match the size of the new patch sheet. */
    public void patchDevices(List<Device> devices) {
        int length = devices.size();
        if(length > 0) {
            this.devices = new Device[length];
            for(int i=0; i<this.devices.length; i++) {
                this.devices[i] = devices.get(i);
            }
        } else {
            this.devices = null;
        }
        this.initPixels(length);
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

    public Pixel[] render() {
        // TODO spell out borrowing contract for render! borrow must not modify.
        // Probably do this with an (Immutable) Pixel & MutablePixel.
        return this.pixels;
    }

    /** levelChanged notifications are sent by Layer when a fader is turned up
     *  past 0%. Most effects don't care, but some will want to rewind when
     *  that happens.
     */
    public void levelChanged(float oldLevel, float newLevel) {}

}
