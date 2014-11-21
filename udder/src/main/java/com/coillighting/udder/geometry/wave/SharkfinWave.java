package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.geometry.Crossfade;

/** A sinusoidally interpolated signal that continuously oscillates from start
 *  to end, then discontinuously jumps back to the start value.
 */
 public class SharkfinWave extends WaveBase {

    public SharkfinWave(double start, double end, long period) {
        super(start, end, period);
    }

    public double interpolate(double x) {
        return Crossfade.sinusoidal(x, start, end);
    }

}
