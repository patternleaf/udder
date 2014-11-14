package com.coillighting.udder.mix;

public interface Animator {

    /** Draw this animator's subscene and/or update its internal state given the
     *  current time. Animating is a separate step from rendering, so animating
     *  doesn't necessarily have anything to do with computing the values of
     *  individual pixels.
     */
    public void animate(TimePoint timePoint);

}
