package com.coillighting.udder.signal;

import com.coillighting.udder.Util;

/** A sinusoidally interpolated signal that continuously oscillates from start
 *  to end, then discontinuously jumps back to the start value.
 */
 public class SharkfinFloatSignal extends FloatSignalBase {

    public SharkfinFloatSignal(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        return Util.crossfadeSinusoidal(x, start, end);
    }

}
