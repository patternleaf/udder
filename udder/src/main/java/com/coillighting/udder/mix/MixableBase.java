package com.coillighting.udder.mix;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.model.Pixel;
import com.coillighting.udder.mix.TimePoint;

/** A concrete base class implementation of Mixable. Removes boilerplate from
 *  Layer and Mixer.
 */
public abstract class MixableBase implements Mixable {

    protected BlendOp blendOp = null; // TODO: default to max mode

    // TODO convert to double
    protected float level = 0.0f; // dark by default so we can fade in, not pop on

    public MixableBase() { }

    public abstract void animate(TimePoint timePoint);

    public abstract void mixWith(Pixel[] otherPixels);

    public BlendOp getBlendOp() {
        return this.blendOp;
    }

    public void setBlendOp(BlendOp blendOp) {
        this.blendOp = blendOp;
    }

    public float getLevel() {
        return this.level;
    }

    public void setLevel(float level) {
        if(level < 0.0f) {
            level = 0.0f;
        } else if(level > 1.0f) {
            level = 1.0f;
        }
        float old = this.level;
        if(old != level) {
            this.level=level;
            this.notifyLevelChanged(old, level);
        }
    }

    /** Subclasses may optionally override this method if they implement
     *  a custom response to level changes.
     */
    protected void notifyLevelChanged(float oldLevel, float newLevel) {}

}