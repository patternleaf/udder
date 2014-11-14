package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.Util;

/** A linearly interpolated signal that continuously oscillates from start to
 *  end, looping back discontinuously to the start value when the period has
 *  elapsed.
 */
public class SawtoothWave extends WaveBase {

    public SawtoothWave(double start, double end, long period) {
        super(start, end, period);
    }

    public double interpolate(double x) {
        return Util.crossfadeLinear(x, start, end);
    }

}
