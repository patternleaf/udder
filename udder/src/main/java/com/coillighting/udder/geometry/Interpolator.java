package com.coillighting.udder.geometry;

import java.awt.geom.Point2D;
import java.util.Random;

import com.coillighting.udder.Util;

public class Interpolator {

    private double rootModePower;
    private double powerModePower;

    protected Random random = null;

    public enum Interpolation {
        LINEAR, SINUSOIDAL, ROOT, POWER
    };

    public Interpolator() {
        this(0.5, 2.5);
    }

    /** Construct a new interpolator with specific exponent parameters
     * to the easing curves. 0.5 (square root), 2.5 (squared + a little)
     * are good starting places for transitions on the order of ~5-10 sec,
     * but (0.20, 5.0) is more extremely nonlinear, to put some excitement
     * back into slow queues.
     */
    public Interpolator(double rootModePower, double powerModePower) {
        this.rootModePower = rootModePower;
        this.powerModePower = powerModePower;
        random = new Random();
    }

    /** Reads, but does not write, start and target points.
     * Writes, but does not read, current point.
     */
    public void interpolate2D(Interpolation mode,
                            double pct,
                            Point2D.Double start,
                            Point2D.Double current,
                            Point2D.Double target)
    {
        if (mode == Interpolation.LINEAR) {
            current.x = Util.crossfadeLinear(pct, start.x, target.x);
            current.y = Util.crossfadeLinear(pct, start.y, target.y);
        } else if(mode == Interpolation.SINUSOIDAL) {
            current.x = Util.crossfadeSinusoidal(pct, start.x, target.x);
            current.y = Util.crossfadeSinusoidal(pct, start.y, target.y);
        } else if(mode == Interpolation.ROOT) {
            current.x = Util.crossfadeExponential(pct, rootModePower, start.x, target.x);
            current.y = Util.crossfadeExponential(pct, rootModePower, start.y, target.y);
        } else if(mode == Interpolation.POWER) {
            current.x = Util.crossfadeExponential(pct, powerModePower, start.x, target.x);
            current.y = Util.crossfadeExponential(pct, powerModePower, start.y, target.y);
        } else {
            throw new IllegalArgumentException("Unsupported interpolation " + mode);
        }
    }

    /** Good values are 10, 50, and 80, in that order. */
    public Interpolation randomMode(int threshold0, int threshold1, int threshold2) {
        Interpolator.Interpolation mode;
        int r = random.nextInt(100);
        if(r < threshold0) {
            mode = Interpolator.Interpolation.LINEAR;
        } else if(r < threshold1) {
            mode = Interpolator.Interpolation.SINUSOIDAL;
        } else if(r < threshold2) {
            mode = Interpolator.Interpolation.ROOT;
        } else {
            mode = Interpolator.Interpolation.POWER;
        }
        return mode;
    }

    public double getRootModePower() {
        return rootModePower;
    }

    public void setRootModePower(double rootModePower) {
        this.rootModePower = rootModePower;
    }

    public double getPowerModePower() {
        return powerModePower;
    }

    public void setPowerModePower(double powerModePower) {
        this.powerModePower = powerModePower;
    }
}