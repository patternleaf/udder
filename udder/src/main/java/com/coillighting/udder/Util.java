package com.coillighting.udder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO see whether using the 32 bit vs 64 bit versions are really any different
// on the Raspi.
public class Util {

    // TODO see if there are already Boon equivalents for these - there likely are.
    /** String.join is available only with Java 1.8. We support 1.7.
     *
     *  Format a list of items as strings, separated by the given conjunction.
     */
    static public String join(List<? extends Object> items, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object x : items) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            if(x == null) {
                sb.append("<null>");
            } else {
                sb.append(x.toString());
            }
        }
        return sb.toString();
    }

    public static <T extends Comparable<? super T>> List<T> sorted(Collection<T> c) {
        ArrayList<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

    /** Linearly interpolate x, a value between 0 and 1, between two points,
     *  (0, y0) and (1, y1). If you supply an x that is out of range, the
     *  result will likewise be out of range.
     */
    public static double crossfadeLinear(double x, double y0, double y1) {
        return y0 + x * (y1 - y0);
    }

    /** A 32-bit version of crossfadeLinear. */
    public static float crossfadeLinear(float x, float y0, float y1) {
        return y0 + x * (y1 - y0);
    }

    /** For a fast attack followed by a slower follow-through, try an exponent
     *  of 0.5 or 0.25. For a gradual attack with an accelerating follow-up,
     *  try an exponent of 2-5. This is not really the shapliest easing curve
     *  curve out there, but it's good to have in the arsenal.
     */
    public static double crossfadeExponential(double x, double exponent, double y0, double y1) {
        double balance = Math.pow(x, exponent);
        return crossfadeLinear(balance, y0, y1);
    }

    /** A mostly 32-bit version of crossfadeExponential. */
    public static float crossfadeExponential(float x, float exponent, float y0, float y1) {
        float balance = (float) Math.pow((double) x, (double) exponent);
        return crossfadeLinear(balance, y0, y1);
    }

    /** Use a sinewave to approximate a sigmoidal cross-fade.
     *  Submit this expression to wolframalpha.com to see the resulting curve:
     *      0.5*(1 + (sin((pi*(x-0.5)))))
     *  Direct link:
     *      http://www.wolframalpha.com/input/?i=0.5*%281+%2B+%28sin%28%28pi*%28x-0.5%29%29%29%29%29+from+-0.25+to+1.5
     */
    public static double crossfadeSinusoidal(double x, double y0, double y1) {
        // Map an x in range 0..1 to an xx in range -pi..0.
        double xx = (x - 0.5) * Math.PI;
        double yy = Math.sin(xx);

        // Map a yy in range -1..1 to a balance in range y0..y1.
        double balance = 0.5 * (yy + 1.0);
        return crossfadeLinear(balance, y0, y1);
    }

    /** A mostly 32-bit version of crossfadeExponential. */
    public static float crossfadeSinusoidal(float x, float y0, float y1) {
        // Map an x in range 0..1 to an xx in range -pi..0.
        double xx = ((double)x - 0.5) * Math.PI;
        float yy = (float) Math.sin(xx);

        // Map a yy in range -1..1 to a balance in range y0..y1.
        float balance = 0.5f * (yy + 1.0f);
        return crossfadeLinear(balance, y0, y1);
    }

}
