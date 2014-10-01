package com.coillighting.udder;

/** We typically construct scenes from multiple conflicting Mixable sources,
 *  but there is a single physical display. Resolving conflicts between sources
 *  is called blending.
 */
public interface Mixable extends Effect {

	/** One step in the mixdown loop. Compare otherPixels to this Animator's
	 *  version of the scene and resolve conflicts according to this Mixable's
	 *  BlendOp, and in light of this Mixable's current level.
	 *  (TODO: BlendOp soon to be BlendMode. Not all interesting blend modes
	 *  are channel-agnostic and channel-homogeneous.)
	 *  Write any changes back to otherPixels, an array which represents the
	 *  developing frame.
	 */
	public void mixWith(Pixel[] otherPixels);

	public BlendOp getBlendOp();
	public void setBlendOp(BlendOp blendOp);

	/** A scaling factor. Valid values are in the range [0.0, 1.0]. The
	 *  interpretation of this value is implementation-specific, but
	 *  convention dictates that a Mixable object whose level is 0.0 should
	 *  not affect the scene, whereas a Mixable with a level of 1.0 should
	 *  maximally affect the scene.
	 *
	 *  This is your layer's volume knob, a.k.a. its submaster.
	 */
	public float getLevel();
	public void setLevel(float level);

}
