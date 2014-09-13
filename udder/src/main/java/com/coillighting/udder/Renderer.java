package com.coillighting.udder;


/** Something which draws Pixels. */
public interface Renderer {

	/** Render this object's current state as a pixel array. Normally, each
	 *  Renderer is also an Animator, and each call to render() follows a call
	 *  to animate(TimePoint). See Effect.
	 */
	public Pixel[] render();

}
