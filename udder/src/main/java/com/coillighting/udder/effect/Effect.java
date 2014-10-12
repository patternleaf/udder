package com.coillighting.udder.effect;

import com.coillighting.udder.Animator;
import com.coillighting.udder.Patchable;
import com.coillighting.udder.Renderer;
import com.coillighting.udder.Stateful;

/** When your drawing routine can be patched, animated, and rendered, then
 *  it is ready to be loaded onto a Layer as an Effect.
 */
public interface Effect extends Animator, Patchable, Renderer, Stateful {

}
