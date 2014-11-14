package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.Util;

/** A sinusoidally interpolated signal that continuously oscillates between two
 *  values, with smooth corners at the values themselves.
 */
 public class SinusoidalFloatWave extends FloatWaveBase {

    public SinusoidalFloatWave(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        // This is a funny way to implement this, but I want to give
        // crossfadeSinusoidal a little exercise before simplifying it.
        // Alternately, I might add a ramp-up balance/ramp-down balance
        // param to this class, for use as a simple impulse envelope. (TODO)
        float x0;
        float x1;
        if(x <= 0.5f) {
            x0 = start;
            x1 = end;
        } else {
            x -= -0.5f;
            x0 = end;
            x1 = start;
        }
        x *= 2.0f;
        return Util.crossfadeSinusoidal(x, x0, x1);
    }

}
