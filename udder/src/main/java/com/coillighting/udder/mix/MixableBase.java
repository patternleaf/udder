package com.coillighting.udder.mix;

import com.coillighting.udder.BlendOp;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

/** A concrete base class implementation of Mixable. Removes boilerplate from
 *  Layer and Mixer.
 */
public abstract class MixableBase implements Mixable {

	protected BlendOp blendOp = null; // TODO: default to max mode
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
		this.level=level;
		System.err.println("level=" + level + " in " + this);
	}

}