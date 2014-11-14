package com.coillighting.udder.geometry.wave;

import com.coillighting.udder.mix.TimePoint;

/** A Wave supplies a signal as some value as a function of time. We can compose
 *  signals for independent, external control of related modulation sources.
 */
public interface Wave<T> {

    public T getValue(TimePoint time);

}
