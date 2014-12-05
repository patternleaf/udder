package com.coillighting.udder.mix;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.model.Pixel;
import com.coillighting.udder.mix.TimePoint;

/** A concrete base class implementation of Mixable. Removes boilerplate from
 *  Layer and Mixer.
 */
public abstract class MixableBase implements Mixable {

    // If you don't know what BlendOp to set, start with MaxBlendOp.
    protected BlendOp blendOp = null;

    protected double level = 0.0; // dark by default so we can fade in, not pop on

    public MixableBase() { }

    public abstract void animate(TimePoint timePoint);

    public abstract void mixWith(Pixel[] otherPixels);

    public BlendOp getBlendOp() {
        return this.blendOp;
    }

    public void setBlendOp(BlendOp blendOp) {
        this.blendOp = blendOp;
    }

    public double getLevel() {
        return this.level;
    }

    public void setLevel(double level) {
        if(level < 0.0) {
            level = 0.0;
        } else if(level > 1.0) {
            level = 1.0;
        }
        double old = this.level;
        if(old != level) {
            this.level=level;
            this.notifyLevelChanged(old, level);
        }
    }

    /** Subclasses may optionally override this method if they implement
     *  a custom response to level changes.
     */
    protected void notifyLevelChanged(double oldLevel, double newLevel) {}

}