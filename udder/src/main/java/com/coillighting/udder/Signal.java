package com.coillighting.udder;


/** A Signal supplies some value as a function of time. We can compose Signals
 *  for independent, external control of related modulation sources.
 */
public interface Signal<T> {

    public T getValue(TimePoint time);

}
