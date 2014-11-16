package com.coillighting.udder.mix;

import com.coillighting.udder.infrastructure.Stateful;

/** We could probably work around this composite interface with some
 * advice on generics syntax -- specifically we need to instantiate
 * a list that holds anything that implements both Stateful and Animator
 * in Mixer.subscribers.
 */
public interface StatefulAnimator extends Stateful, Animator {}
