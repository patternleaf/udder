package com.coillighting.udder.geometry;

public class Crossfade {

    /** Linearly interpolate x, a value between 0 and 1, between two points,
     *  (0, y0) and (1, y1). If you supply an x that is out of range, the
     *  result will likewise be out of range.
     */
    public static final double linear(double x, double y0, double y1) {
        return y0 + x * (y1 - y0);
    }

    /** A 32-bit version of Crossfade.linear. */
    public static final float linear(float x, float y0, float y1) {
        return y0 + x * (y1 - y0);
    }

    /** For a fast attack followed by a slower follow-through, try an exponent
     *  of 0.5 or 0.25. For a gradual attack with an accelerating follow-up,
     *  try an exponent of 2-5. This is not really the shapliest easing curve
     *  curve out there, but it's good to have in the arsenal.
     */
    public static final double exponential(double x, double exponent, double y0, double y1) {
        double balance = Reshape.exponential(x, exponent);
        return linear(balance, y0, y1);
    }

    /** A mostly 32-bit version of Crossfade.exponential. */
    public static final float exponential(float x, float exponent, float y0, float y1) {
        float balance = Reshape.exponential(x, exponent);
        return linear(balance, y0, y1);
    }

    /** Use a sinewave to approximate a sigmoidal cross-fade curve.
     *  Submit this expression to wolframalpha.com to see the resulting curve:
     *      0.5*(1 + (sin((pi*(x-0.5)))))
     *  Direct link:
     *      http://www.wolframalpha.com/input/?i=0.5*%281+%2B+%28sin%28%28pi*%28x-0.5%29%29%29%29%29+from+-0.25+to+1.5
     */
    public static final double sinusoidal(double x, double y0, double y1) {
        double balance = Reshape.sinusoidal(x);
        return linear(balance, y0, y1);
    }

    /** A mostly 32-bit version of crossfadeExponential. */
    public static final float sinusoidal(float x, float y0, float y1) {
        float balance = Reshape.sinusoidal(x);
        return linear(balance, y0, y1);
    }

    // TODO logarithmic mode!

}
