package com.coillighting.udder.geometry;

public class Reshape {

    /** A version of Crossfade.exponential optimized for normalized scenarios
     *  where y0=0, y1=1, and x is between 0 and 1 inclusive. Reduces to
     *  x^exponent.
     *
     *  WolframAlpha query for reshapeExponential(x, 2.0):
     *      (x**2) from 0 to 1
     *      http://www.wolframalpha.com/input/?i=%28x**2%29+from+0+to+1
     *
     *  WolframAlpha query for reshapeExponential(x, 0.5):
     *      (x**0.25) from 0 to 1
     *      http://www.wolframalpha.com/input/?i=%28x**0.25%29+from+0+to+1
     */
    public static final double exponential(double x, double exponent) {
        return Math.pow(x, exponent);
    }

    /** A mostly 32-bit version of Reshape.exponential. */
    public static final float exponential(float x, float exponent) {
        return (float) Math.pow((double) x, (double) exponent);
    }

    /** Like Crossfade.sinusoidal, but optimized for normalized input scenarios
     *  where y0=0, y1=1, and is is between 0 and 1 inclusive.
     *
     *  WolframAlpha query for reshapeSinusoidal(x):
     *      0.5 * (1 + sin((x-0.5)*pi)) from 0 to 1
     *      http://www.wolframalpha.com/input/?i=0.5+*+%281+%2B+sin%28%28x-0.5%29*pi%29%29+from+0+to+1
     */
    public static final double sinusoidal(double x) {
        // Map an x in range 0..1 to an xx in range -pi..0.
        double xx = (x - 0.5) * Math.PI;
        double yy = Math.sin(xx);

        // Map a yy in range -1..1 to a balance in range 0..1.
        return 0.5 * (yy + 1.0);
    }

    /** A mostly 32-bit version of reshapeSinusoidal. */
    public static final float sinusoidal(float x) {
        // Map an x in range 0..1 to an xx in range -pi..0.
        double xx = ((double)x - 0.5) * Math.PI;
        float yy = (float) Math.sin(xx);

        // Map a yy in range -1..1 to a balance in range y0..y1.
        return 0.5f * (yy + 1.0f);
    }

    // TODO logarithmic mode!
}