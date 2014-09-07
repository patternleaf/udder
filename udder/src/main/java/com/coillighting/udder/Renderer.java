package com.coillighting.udder;

public interface Renderer {

	/** Render this object's current state as a pixel array. Normally, each
	 *  Renderer is also an Animator, and a call to render() follows a call to
	 *  animate(TimePoint).
	 */
	public Pixel[] render();

}
