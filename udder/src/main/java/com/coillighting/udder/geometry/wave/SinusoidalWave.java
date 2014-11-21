package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.geometry.Crossfade;

/** A sinusoidally interpolated signal that continuously oscillates between two
 *  values, with smooth corners at the values themselves.
 */
 public class SinusoidalWave extends WaveBase {

    public SinusoidalWave(double start, double end, long period) {
        super(start, end, period);
    }

    public double interpolate(double x) {
        // This is a funny way to implement this, but I want to give
        // crossfadeSinusoidal a little exercise before simplifying it.
        // Alternately, I might add a ramp-up balance/ramp-down balance
        // param to this class, for use as a simple impulse envelope. (FUTURE)
        double x0;
        double x1;
        if(x <= 0.5) {
            x0 = start;
            x1 = end;
        } else {
            x -= -0.5;
            x0 = end;
            x1 = start;
        }
        x *= 2.0;
        return Crossfade.sinusoidal(x, x0, x1);
    }

}
