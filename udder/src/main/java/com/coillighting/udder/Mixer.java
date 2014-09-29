package com.coillighting.udder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import com.coillighting.udder.MaxBlendOp;
import com.coillighting.udder.Layer;
import com.coillighting.udder.MixableBase;
import com.coillighting.udder.Mixable;


/** A concrete scenegraph is implemented as a Mixer with one or more Layers.
 *  Each layer holds an Animator, ordinarily an effect plug-in which draws
 *  part of the scene. The Mixer then composites the complete scene by blending
 *  each layer in turn with the output of earlier blend operations.
 */
public class Mixer extends MixableBase implements Mixable, Iterable<Mixable> {

	/** In order of composition, i.e. first element is the background layer,
	 *  last element is the foreground layer.
	 */
	private ArrayList<Mixable> layers;
	private Pixel[] pixels; // the developing frame
	private int deviceCount = 0;

	public Mixer(Collection<Mixable> layers) {
		this.layers = new ArrayList(layers);
		this.setBlendOp(new MaxBlendOp());
	}

	public static Class getStateClass() {
		return Object.class; // TODO
	}

	public Mixable getLayer(int index) {
		return this.layers.get(index);
	}

	public Iterator<Mixable> iterator() {
		return this.layers.iterator();
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
		this.pixels = new Pixel[this.deviceCount];
		for(int i=0; i<this.pixels.length; i++) {
			this.pixels[i] = new Pixel(0.0f, 0.0f, 0.0f);
		}
		for(Mixable layer : this) {
			layer.mixWith(this.pixels);
		}
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

}
