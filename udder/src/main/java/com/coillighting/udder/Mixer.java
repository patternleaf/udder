package com.coillighting.udder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector; // TODO switch to arraylist?

import com.coillighting.udder.MaxBlendOp;
import com.coillighting.udder.Layer;
import com.coillighting.udder.MixableBase;
import com.coillighting.udder.Mixable;


public class Mixer extends MixableBase implements Mixable, Iterable<Mixable> {

	/** In order of composition, i.e. first element is the background layer,
	 *  last element is the foreground layer.
	 */
	private Vector<Mixable> layers; // TODO switch to arraylist?
	private Pixel[] pixels; // the developing frame

	public Mixer(Collection<Mixable> layers) {
		this.layers = new Vector(layers);
		this.setBlendOp(new MaxBlendOp());
	}

	public Mixable getLayer(int index) {
		return this.layers.get(index);
	}

	public Iterator<Mixable> iterator() {
		return this.layers.iterator();
	}

	public void animate(TimePoint timePoint) {
		for(Mixable layer : this) {
			layer.animate(timePoint);
		}
	}

	public void mixWith(Pixel[] otherPixels) {
		Arrays.fill(this.pixels, 0.0);
		for(Mixable layer : this) {
			layer.mixWith(this.pixels);
		}
	}

}
