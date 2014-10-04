package com.coillighting.udder;


/** Base class implementing a constant signal. */
public class ConstantFloatSignal implements Signal<Float> {

    protected float value = 0.0f;

    public ConstantFloatSignal(float value) {
        this.value = value;
    }

    public float getVal(TimePoint time) {
        return value;
    }

    public Float getValue(TimePoint time) {
        return value;
    }

}
