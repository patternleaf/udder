package com.coillighting.udder;


/** A linearly interpolated signal that continuously oscillates from start to
 *  end, looping back discontinuously to the start value when the period has
 *  elapsed.
 */
public class SawtoothFloatSignal extends FloatSignalBase {

    public SawtoothFloatSignal(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        return Util.crossfadeLinear(x, start, end);
    }

}
