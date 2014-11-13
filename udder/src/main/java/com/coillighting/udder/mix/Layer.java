package com.coillighting.udder.mix;

import java.lang.UnsupportedOperationException;
import java.util.List;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.effect.Effect;
import com.coillighting.udder.Device;
import com.coillighting.udder.TimePoint;

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
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        this.setLevel(((LayerState)state).getLevel());
    }

    public void animate(TimePoint timePoint) {
        this.effect.animate(timePoint);
    }

    public Pixel[] render() {
        return this.effect.render();
    }

    public void mixWith(Pixel[] canvasPixels) {
        String before = canvasPixels[0].toString(); //TEMP
        Pixel[] myPixels = this.render();
        int length = myPixels.length;
        int min = canvasPixels.length > myPixels.length ? myPixels.length : canvasPixels.length;
        for(int i=0; i<min; i++) {
            canvasPixels[i].blendWith(myPixels[i], this.level, this.blendOp);
        }
    }

    public void patchDevices(List<Device> devices) {
        this.effect.patchDevices(devices);
    }

    public Effect getEffect() {
        return this.effect;
    }

    public String toString() {
        return this.name;
    }

}
