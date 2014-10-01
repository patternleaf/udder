package com.coillighting.udder;

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

	public void scale(float scale) {
		this.r *= scale;
		this.g *= scale;
		this.b *= scale;
	}

	public void setColor(Pixel pixel) {
		if(pixel == null) {
			throw new NullPointerException("A null pixel has no color.");
		} else {
			this.setColor(pixel.r, pixel.g, pixel.b);
		}
	}

	public void blendWith(Pixel foreground, float level, BlendOp blendOp) {
		if(blendOp == null) {
			throw new NullPointerException("BlendOp is required.");
		} else if(foreground != null && level > 0.0) {
			level = this.clipChannel(level);
			this.setColor(
				blendOp.blend(this.r, level * foreground.r),
				blendOp.blend(this.g, level * foreground.g),
				blendOp.blend(this.b, level * foreground.b)
			);
		}
		else if(foreground == null) { // TEMP- DEBUG
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

	public int toRGBA() {
		float conv = 255.99f;
		int rr = (int)(this.r * conv);
		int gg = (int)(this.g * conv);
		int bb = (int)(this.b * conv);
		int aa = 0xFF; // placeholder
		return 0x00000000 | (rr << 24) | (gg << 16) | (bb << 8) | aa;
	}

	public void setColor(int rgba) {
		float conv = 255.0f;
		int rr = (rgba >> 24) & 0xFF;
		int gg = (rgba >> 16) & 0xFF;
		int bb = (rgba >> 8) & 0xFF;
		this.r = (float)rr / conv;
		this.g = (float)gg / conv;
		this.b = (float)bb / conv;
	}

	public static Pixel fromRGBA(int rgba) {
		float conv = 255.0f;
		int rr = (rgba >> 24) & 0xFF;
		int gg = (rgba >> 16) & 0xFF;
		int bb = (rgba >> 8) & 0xFF;
		return new Pixel(
			(float)rr / conv,
			(float)gg / conv,
			(float)bb / conv);
	}
}
