package com.coillighting.udder;

import com.coillighting.udder.BlendOp;

/** A simple data structure for representing high-resolution RGB pixel data.
 *  We eventually mix down to 8 bit RGB, but we animate and mix in this high
 *  resolution (3x float) space. We have this luxury because our fixtures are
 *  so few.
 *
 *  We use the 32 bit float type rather than the 64 bit float type in hopes
 *  that it will speed processing on lightweight 32 bit devices.
 */
public class Pixel {

	/** These are public for fast, direct access. Use with caution. */
	// FIXME the json mapper is not using the standard constructor for this
	public float r=0.0f;
	public float g=0.0f;
	public float b=0.0f;

	public Pixel() {}

	public Pixel(float r, float g, float b) {
		this.setColor(r, g, b);
	}

	public Pixel(Pixel pixel) {
		this.setColor(pixel.r, pixel.g, pixel.b);
	}

	public void setColor(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public void setColor(Pixel pixel) {
		if(pixel == null) {
			throw new NullPointerException("A null pixel has no color.");
		} else {
			this.setColor(pixel.r, pixel.g, pixel.b);
		}
	}

	public void blendWith(Pixel foreground, BlendOp blendOp) {
		if(blendOp == null) {
			throw new NullPointerException("BlendOp is required.");
		} else if(foreground != null) {
			this.setColor(
				blendOp.blend(this.r, foreground.r),
				blendOp.blend(this.g, foreground.g),
				blendOp.blend(this.b, foreground.b));
		}
		else { // TEMP- DEBUG
			System.err.println("Warning: null foreground");
		}
	}

	public boolean equals(Pixel pixel) {
		return pixel != null && pixel.r == this.r && pixel.g == this.g
			&& pixel.b == this.b;
	}

	public void clip() {
		this.r = this.clipChannel(this.r);
		this.g = this.clipChannel(this.g);
		this.b = this.clipChannel(this.b);
	}

	protected static final float clipChannel(float value) {
		if(value <= 0.0f) {
			return 0.0f;
		} else if(value >= 1.0f) {
			return 1.0f;
		} else {
			return value;
		}
	}

	public String toString() {
		return "Pixel(" + r + ", " + g + ", " + b + ")";
	}
}
