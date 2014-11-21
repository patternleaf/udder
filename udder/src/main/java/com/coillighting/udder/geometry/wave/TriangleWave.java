package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.geometry.Crossfade;

/** A linearly interpolated signal that continuously oscillates between two
 *  values, with sharp corners at the values themselves.
 */
 public class TriangleWave extends WaveBase {

    public TriangleWave(double start, double end, long period) {
        super(start, end, period);
    }

    public double interpolate(double x) {
        double x0;
        double x1;
        if(x <= 0.5) {
            x0 = start;
            x1 = end;
        } else {
            x -= 0.5;
            x0 = end;
            x1 = start;
        }
        x *= 2.0;
        return Crossfade.linear(x, x0, x1);
    }

}
