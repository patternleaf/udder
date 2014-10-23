package com.coillighting.udder;

import com.coillighting.udder.blend.BlendOp;

/** A simple data structure for representing high-resolution RGB pixel data.
 *  We eventually mix down to 8 bit RGB, but we animate and mix in this high
 *  resolution (3x float) space. We have this luxury because our fixtures are
 *  so few.
 *
 *  We use the 32 bit float type rather than the 64 bit float type in hopes
 *  that it will speed processing on lightweight 32 bit devices.
 */
public class Pixel {

    /** These are public for fast, direct access. Use with caution.
     *  TODO: just make 'em protected?
     */
    // FIXME the json mapper is not using the standard constructor for these.
    public float r=0.0f;
    public float g=0.0f;
    public float b=0.0f;

    public static Pixel black() {
        return new Pixel(0.0f, 0.0f, 0.0f);
    }

    public static Pixel white() {
        return new Pixel(1.0f, 1.0f, 1.0f);
    }

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

    /** If you don't know what blendOp to use, just use MaxBlendOp until you
     *  have time to experiment with other options.
     *
     *  TODO A variant that takes an RGB BlendMode (per-channel blendops).
     */
    public void blendWith(Pixel foreground, float level, BlendOp blendOp) {
        if(blendOp == null) {
            throw new NullPointerException("BlendOp is required.");
        } else if(foreground != null) {
            if(level > 0.0) {
                float rr = blendOp.blend(this.r, foreground.r);
                float gg = blendOp.blend(this.g, foreground.g);
                float bb = blendOp.blend(this.b, foreground.b);
                if(level < 1.0) {
                    // If level isn't 100%, merge the background with the
                    // blended color according the balance prescribed by level.
                    final float blendedScale = Pixel.clipChannel(level);
                    final float bgScale = 1.0f - blendedScale;

                    rr = blendedScale * rr + bgScale * this.r;
                    gg = blendedScale * gg + bgScale * this.g;
                    bb = blendedScale * bb + bgScale * this.b;
                }
                this.setColor(rr, gg, bb);
            }
        }
    }

    public boolean equals(Pixel pixel) {
        return pixel != null && pixel.r == this.r && pixel.g == this.g
            && pixel.b == this.b;
    }

    public void clip() {
        this.r = Pixel.clipChannel(this.r);
        this.g = Pixel.clipChannel(this.g);
        this.b = Pixel.clipChannel(this.b);
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

    /** Some loss of precision is inevitable. Include alpha. */
    public String toHexRGBA() {
        return String.format("%08X", this.toRGBA() & 0xFFFFFFFF);
    }

    /** Some loss of precision is inevitable. Skip alpha. */
    public String toHexRGB() {
        return String.format("%06X", this.toRGB() & 0xFFFFFF);
    }

    /** Return an int approximation of this pixel value, encoded 0xRRGGBB.
     *  Skip alpha.
     */
    public int toRGB() {
        return this.toRGBA() >> 8;
    }

    /** Return an int approximation of this pixel value, encoded 0xRRGGBBAA.
     *  Include alpha.
     */
    public int toRGBA() {
        float conv = 255.99f;
        int rr = (int)(this.r * conv);
        int gg = (int)(this.g * conv);
        int bb = (int)(this.b * conv);
        int aa = 0xFF; // placeholder
        return 0x00000000 | (rr << 24) | (gg << 16) | (bb << 8) | aa;
    }

    /** Ignore alpha for now. (TODO) */
    public void setColor(int rgba) {
        float conv = 255.0f;
        int rr = (rgba >> 24) & 0xFF;
        int gg = (rgba >> 16) & 0xFF;
        int bb = (rgba >> 8) & 0xFF;
        this.r = (float)rr / conv;
        this.g = (float)gg / conv;
        this.b = (float)bb / conv;
    }

    /** Ignore alpha for now. (TODO). */
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
