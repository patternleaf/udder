package com.coillighting.udder;

import com.coillighting.udder.BlendOp;

/** A simple data structure for representing high-resolution RGB pixel data.
 *  We eventually mix down to 8 bit RGB, but we animate and mix in this high
 *  resolution (3x double) space. We have this luxury because our fixtures are
 *  so few.
 */
public class Pixel {

	/** These are public for fast, direct access. */
	public double r=0.0;
	public double g=0.0;
	public double b=0.0;

	public Pixel() {}

	public Pixel(double r, double g, double b) {
		this.setColor(r, g, b);
	}

	public Pixel(Pixel pixel) {
		this.setColor(pixel.r, pixel.g, pixel.b);
	}

	public void setColor(double r, double g, double b) {
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
			this.r = blendOp.blend(this.r, foreground.r);
			this.g = blendOp.blend(this.g, foreground.g);
			this.b = blendOp.blend(this.b, foreground.b);
		}
		else {
			// TEMP- DEBUG
			System.err.println("Warning: null foreground");
		}
	}

	public boolean equals(Pixel pixel) {
		return pixel != null && pixel.r == this.r && pixel.g == this.g
			&& pixel.b == this.b;
	}

}
