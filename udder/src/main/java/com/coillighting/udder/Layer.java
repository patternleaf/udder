package com.coillighting.udder;

import java.lang.UnsupportedOperationException;
import java.util.List;

import com.coillighting.udder.Effect;
import com.coillighting.udder.MixableBase;
import com.coillighting.udder.Mixable;


/** A Mixer is typically composed of several Layers. Each Layer is capable of
 *  animating and rendering the whole scene, so the parent Mixer is responsible
 *  for resolving conflicts between each Layer's version of the scene by
 *  blending them together. This step is called mixdown, and we implement it as
 *  a series of mixWith(..) calls.
 */
public class Layer extends MixableBase implements Effect, Mixable {

	/** A human-readable display name for this Layer. (Keep it short.) */
	private String name;

	/** Delegate animations to this plug-in effect. */
	private Effect effect;

	public Layer(String name, Effect effect) {
		if(effect == null) {
			throw new NullPointerException("Layer requires an Effect to animate and render pixels.");
		} else if(name == null) {
			name = "Untitled";
		}
		this.name = name;
		this.effect = effect;
	}

	public static Class getStateClass() {
		return Object.class; // TODO
	}

	public Object getState() {
		// TODO
		return null;
	}

	public void setState(Object state) throws ClassCastException {
		// TODO
	}

	public void animate(TimePoint timePoint) {
		this.effect.animate(timePoint);
	}

	public Pixel[] render() {
		return this.effect.render();
	}

	public void mixWith(Pixel[] otherPixels) {
		String before = otherPixels[0].toString();
		Pixel[] myPixels = this.render();
		int length = myPixels.length;
		for(int i=0; i<otherPixels.length && i<myPixels.length; i++) {
			otherPixels[i].blendWith(myPixels[i], this.level, this.blendOp);
		}
		String fg = myPixels[0].toString();
		String after = otherPixels[0].toString();
		System.err.println("mixWith: " + before + " + " + fg + " @" + this.level + " = " + after);
	}

	public void patchDevices(List<Device> devices) {
		this.effect.patchDevices(devices);
	}

	public Effect getEffect() {
		return this.effect;
	}

}
