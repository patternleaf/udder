package com.coillighting.udder;

import com.coillighting.udder.BlendOp;
import com.coillighting.udder.Layer;
import com.coillighting.udder.Mixable;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;


public abstract class MixableBase implements Mixable {

	private BlendOp blendOp;
	private double level;

	public MixableBase() {
		this.blendOp = null; // TODO: default to max mode
		this.level = 0.0; // dark by default so we can fade in, not pop on
	}

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
		this.level=level;
	}

}