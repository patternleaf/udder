package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.Util;

/** A linearly interpolated signal that continuously oscillates from start to
 *  end, looping back discontinuously to the start value when the period has
 *  elapsed.
 */
public class SawtoothFloatWave extends FloatWaveBase {

    public SawtoothFloatWave(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        return Util.crossfadeLinear(x, start, end);
    }

}
