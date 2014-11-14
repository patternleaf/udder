package com.coillighting.udder.geometry.wave;


/** An uninterpolated signal that discontinuously switches between two
 *  values.
 */
 public class SquareFloatWave extends FloatWaveBase {

    public SquareFloatWave(float start, float end, long period) {
        super(start, end, period);
    }

    public float interpolate(float x) {
        if(x <= 0.5f) {
            return start;
        } else {
            return end;
        }
    }

}
