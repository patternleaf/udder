package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.mix.TimePoint;

/** Base class implementing a constant signal. */
public class ConstantWave implements Wave<Double> {

    protected double value = 0.0;

    public ConstantWave(double value) {
        this.value = value;
    }

    public double getVal(TimePoint time) {
        return value;
    }

    public Double getValue(TimePoint time) {
        return value;
    }

}
