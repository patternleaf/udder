package com.coillighting.udder;


/** A colorspace-agnostic, per-channel blending function. We build up
 *  colorspace-specific blend modes by combining blend ops. You can construct
 *  the simplest and most useful multichannel blend modes (such as RGB max, min,
 *  multiply, and add) by assigning the same BlendOp independently to each
 *  channel (R, G, B).
 */
public interface BlendOp {

	/** Blend the channel value in foreground with the value in background, and
	 *  return the result.
	 */
	public float blend(float background, float foreground);

}
