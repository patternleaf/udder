package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.mix.TimePoint;

/** Abstract base class for removing boilerplate from the implementation of
 *  periodic, high-resolution floating-point signal generators.
 */
public abstract class WaveBase implements Wave<Double> {

    protected double start = 0.0;
    protected double end = 0.0;
    protected long period = 0;

    public WaveBase(Double start, double end, long period) {
        this.start = start;
        this.end = end;
        this.period = period;
    }

    public Double getValue(TimePoint time) {
        return this.getVal(time);
    }

    public double getVal(TimePoint time) {
        if(period <= 0.0) {
            return start;
        } else {
            // Normalize time to unit length and select + or - ramp.
            long dt = time.sceneTimeMillis() % period;
            double balance = (double) dt / (double) period;
            return this.interpolate(balance);
        }
    }

    public abstract double interpolate(double x);
}
