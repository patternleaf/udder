package com.coillighting.udder.mix;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

/** A concrete scenegraph is implemented as a Mixer with one or more Layers.
 *  Each layer holds an Animator, ordinarily an effect plug-in which draws
 *  part of the scene. The Mixer then composites the complete scene by blending
 *  each layer in turn with the output of earlier blend operations.
 */
public class Mixer extends MixableBase implements Mixable, Iterable<Mixable> {

    /** In order of composition, i.e. first element is the background layer,
     *  last element is the foreground layer.
     */
    protected ArrayList<Mixable> layers;
    protected Pixel[] pixels; // the developing frame
    protected int deviceCount = 0;
    protected boolean verbose = false;

    public Mixer(Collection<Mixable> layers) {
        this.layers = new ArrayList<Mixable>(layers);
        this.setBlendOp(new MaxBlendOp());
    }

    public Class getStateClass() {
        // TODO - let users set all levels for all layers at once
        return LayerState.class;
    }

    public Object getState() {
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        this.setLevel(((LayerState)state).getLevel());
    }

    public Mixable getLayer(int index) throws IndexOutOfBoundsException {
        return this.layers.get(index);
    }

    public Iterator<Mixable> iterator() {
        return this.layers.iterator();
    }

    public int size() {
        if(this.layers == null) {
            return 0;
        } else {
            return this.layers.size();
        }
    }

    /** For each child Mixable (e.g. Layer), draw the subscene and/or update the
     *  state of the child's (Layer's) animator given the current time. After
     *  animating, all children (Layers) will be ready to render their current
     *  state as Pixels.
     */
    public void animate(TimePoint timePoint) {
        for(Mixable layer : this) {
            layer.animate(timePoint);
        }
    }

    /** Mix the output of each child (Layer), starting with the background and
     *  ending with the foreground.
     */
    public void mixWith(Pixel[] otherPixels) {
        this.pixels = new Pixel[this.deviceCount]; // canvas

        for(int i=0; i<this.pixels.length; i++) {
            this.pixels[i] = new Pixel(0.0f, 0.0f, 0.0f);
        }

        if(this.level > 0.0) {
            for(Mixable layer : this) {
                layer.mixWith(this.pixels);
            }
            if(this.level < 1.0) {
                for(Pixel p: this.pixels) {
                    p.scale(this.level);
                }
            }
        }
        if(this.verbose) System.err.println("mixWith: Mixer @" + this.getLevel() + " = " + this.pixels[0]); // TEMP
    }

    public void patchDevices(List<Device> devices) {
        this.deviceCount = devices.size();
        for(Mixable layer : layers) {
            layer.patchDevices(devices);
        }
    }

    public Pixel[] render() {
        // FIXME make a whole new frame? or copy the developed frame before giving it away? clarify ownership.
        this.mixWith(this.pixels);
        return this.pixels;
    }

    public String toString() {
        return "Mixer";
    }

}
