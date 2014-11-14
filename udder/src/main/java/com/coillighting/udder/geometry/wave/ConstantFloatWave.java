package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.mix.TimePoint;

/** Base class implementing a constant signal. */
public class ConstantFloatWave implements Wave<Float> {

    protected float value = 0.0f;

    public ConstantFloatWave(float value) {
        this.value = value;
    }

    public float getVal(TimePoint time) {
        return value;
    }

    public Float getValue(TimePoint time) {
        return value;
    }

}
