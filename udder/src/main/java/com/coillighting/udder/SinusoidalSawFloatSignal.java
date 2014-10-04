package com.coillighting.udder;


/** A sinusoidally interpolated signal that continuously oscillates from start
 *  to end, then discontinuously jumps back to the start value.
 */
 public class SinusoidalSawFloatSignal extends FloatSignalBase {

    public SinusoidalSawFloatSignal(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        return Util.crossfadeSinusoidal(x, start, end);
    }

}
