package com.coillighting.udder.geometry;

import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Implement a crossfade curve whose shape varies randomly within a
 * user-specified envelope. This is analogous to a DJ crossfader with
 * a second, linked fader for continuously selecting between a repertoire
 * of transition profiles: sharp transitions, smooth linear transitions,
 * smooth nonlinear transitions, ...
 */
public class Interpolator {

    private double rootModePower;
    private double powerModePower;

    protected Random random = null;

    /** ROOT: hold out for as long as possible; short, sharp tail.
     * POWER: fade out to a low level rapidly; long, gradual tail.
     * SINUSOIDAL, LINEAR: see Wolfram Alpha graph links elsewhere.
     * TODO: put graph links here, too.
     */
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
            current.x = Crossfade.linear(pct, start.x, target.x);
            current.y = Crossfade.linear(pct, start.y, target.y);
        } else if(mode == Interpolation.SINUSOIDAL) {
            current.x = Crossfade.sinusoidal(pct, start.x, target.x);
            current.y = Crossfade.sinusoidal(pct, start.y, target.y);
        } else if(mode == Interpolation.ROOT) {
            current.x = Crossfade.exponential(pct, rootModePower, start.x, target.x);
            current.y = Crossfade.exponential(pct, rootModePower, start.y, target.y);
        } else if(mode == Interpolation.POWER) {
            current.x = Crossfade.exponential(pct, powerModePower, start.x, target.x);
            current.y = Crossfade.exponential(pct, powerModePower, start.y, target.y);
        } else {
            throw new IllegalArgumentException("Unsupported interpolation " + mode);
        }
    }

    /**
     *  Pick a random mode, specifying the probability of choosing a given mode.
     *
     *  Good values are 10, 50, and 80, in that order.
     *  Visualize the basic repertoire of transition curves here:
     *
     *  LINEAR:
     *      http://www.wolframalpha.com/input/?i=graph+y%3Dx+from+0+to+1
     *
     *  SINUSOIDAL:
     *      http://www.wolframalpha.com/input/?i=graph+y%3D%28sin%28pi*%28x-0.5%29%29%2B+1%29%2F2+from+0+to+1
     *
     *  ROOT:
     *      where rootModePower = 0.5 (square root):
     *          http://www.wolframalpha.com/input/?i=graph+y%3Dx**0.5+from+0+to+1
     *      where rootModePower = 1/3 (cube root):
     *          http://www.wolframalpha.com/input/?i=graph+y%3Dx**%281%2F3%29+from+0+to+1
     *
     *  POWER:
     *      where powerModePower = 2.0 (quadradic fade-in):
     *          http://www.wolframalpha.com/input/?i=graph+y%3Dx**2+from+0+to+1
     *      where powerModePower = 3.0 (cubic fade-in):
     *          http://www.wolframalpha.com/input/?i=graph+y%3Dx**3+from+0+to+1
     *
     *  Your exponents may vary. Just change rootModePower and/or powerModePower.
     */
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
