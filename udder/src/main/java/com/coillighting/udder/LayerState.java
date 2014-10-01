package com.coillighting.udder;

/** A simple datastructure wrapping layer-related state. This is a mapping
 *  target for the Boon JSON loader. LayerState objects become Command payloads.
 */
public class LayerState {

	// Just level so far, but eventually probably also at least one LFO.
	protected float level = 0.0f;

	public LayerState(float level) {
		this.level = level;
	}

	public float getLevel() {
		return this.level;
	}

}
