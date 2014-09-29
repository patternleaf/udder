package com.coillighting.udder;


/** When your drawing routine can be patched, animated, and rendered, then
 *  it is ready to be loaded onto a Layer as an Effect.
 */
public interface Effect extends Animator, Patchable, Renderer {

	public Object getState();

	public void setState(Object state) throws ClassCastException;

}
