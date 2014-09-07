package com.coillighting.udder;

import java.lang.UnsupportedOperationException;

import com.coillighting.udder.Effect;
import com.coillighting.udder.MixableBase;
import com.coillighting.udder.Mixable;


public class Layer extends MixableBase implements Effect, Mixable {

	/** A human-readable display name for this Layer. (Keep it short.) */
	private String name;

	/** Delegate animations to this plug-in effect. */
	private Effect effect;

	public Layer(String name, Effect effect) {
		if(effect == null) {
			throw new NullPointerException("Layer requires an Effect to animate and render pixels.");
		}
		if(name == null) {
			name = "Untitled";
		}
		this.name = name;
		this.effect = effect;
	}

	public void animate(TimePoint timePoint) {
		this.effect.animate(timePoint);
	}

	public Pixel[] render() {
		return this.effect.render();
	}

	public void mixWith(Pixel[] otherPixels) {
		Pixel[] myPixels = this.render();
		throw new UnsupportedOperationException(
			"TODO - blend r, g, b per pixel");
	}

}
