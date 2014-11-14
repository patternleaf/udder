package com.coillighting.udder.geometry.wave;


/** An uninterpolated signal that discontinuously switches between two
 *  values.
 */
 public class SquareWave extends WaveBase {

    public SquareWave(double start, double end, long period) {
        super(start, end, period);
    }

    public double interpolate(double x) {
        if(x <= 0.5) {
            return start;
        } else {
            return end;
        }
    }

}
