package com.coillighting.udder.mix;

import java.lang.UnsupportedOperationException;
import java.util.List;

import com.coillighting.udder.effect.Effect;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** A Mixer is typically composed of several Layers. Each Layer is capable of
 *  animating and rendering the whole scene, so the parent Mixer is responsible
 *  for resolving conflicts between each Layer's version of the scene by
 *  blending them together. This step is called mixdown, and we implement it as
 *  a series of mixWith(..) calls.
 */
public class Layer extends MixableBase implements Effect, Mixable {

    /** A human-readable display name for this Layer. (Keep it short.) */
    protected String name;

    /** Delegate animations to this plug-in effect. */
    protected Effect effect;

    public Layer(String name, Effect effect) {
        if(effect == null) {
            throw new NullPointerException("Layer requires an Effect to animate and render pixels.");
        } else if(name == null) {
            name = "Untitled";
        }
        this.name = name;
        this.effect = effect;
    }

    public Class getStateClass() {
        return LayerState.class;
    }

    public Object getState() {
        return null; // TODO LayerState
    }

    public void setState(Object state) throws ClassCastException {
        this.setLevel(((LayerState)state).getLevel());
    }

    public void animate(TimePoint timePoint) {
        this.effect.animate(timePoint);
    }

    /** See borrowing contract on Effect. */
    public Pixel[] render() {
        return this.effect.render();
    }

    public void mixWith(Pixel[] canvasPixels) {
        Pixel[] myPixels = this.render();
        int min = canvasPixels.length > myPixels.length ? myPixels.length : canvasPixels.length;
        float lf = (float) this.level;
        for(int i=0; i<min; i++) {
            canvasPixels[i].blendWith(myPixels[i], lf, this.blendOp);
        }
    }

    public void patchDevices(Device[] devices) {
        this.effect.patchDevices(devices);
    }

    public Effect getEffect() {
        return this.effect;
    }

    public String toString() {
        return this.name;
    }

    /** Currently we notify Effects only when the layer goes from off (0%)
     *  to on (>0%). Some effects (currently just Woven) will want to start
     *  from their first cue or reset animation state when that happens.
     */
    protected void notifyLevelChanged(double oldLevel, double newLevel) {
        if(effect != null && oldLevel == 0.0 && newLevel > 0.0) {
            effect.levelChanged(oldLevel, newLevel);
        }
    }

    /** A Layer doesn't currently care when its parent Mixer's level changes. */
    public void levelChanged(double oldLevel, double newLevel) {}

}
