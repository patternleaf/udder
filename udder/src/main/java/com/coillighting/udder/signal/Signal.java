package com.coillighting.udder.signal;

import com.coillighting.udder.mix.TimePoint;

/** A Signal supplies some value as a function of time. We can compose Signals
 *  for independent, external control of related modulation sources.
 */
public interface Signal<T> {

    public T getValue(TimePoint time);

}
