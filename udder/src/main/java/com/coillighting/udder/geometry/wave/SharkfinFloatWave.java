package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.geometry.Crossfade;

/** A sinusoidally interpolated signal that continuously oscillates from start
 *  to end, then discontinuously jumps back to the start value.
 */
 public class SharkfinFloatWave extends FloatWaveBase {

    public SharkfinFloatWave(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        return Crossfade.sinusoidal(x, start, end);
    }

}
