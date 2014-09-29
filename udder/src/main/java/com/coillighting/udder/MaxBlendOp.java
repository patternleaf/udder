package com.coillighting.udder;

import com.coillighting.udder.BlendOp;

public class MaxBlendOp implements BlendOp {

	public float blend(float background, float foreground) {
		return background >= foreground ? background : foreground;
	}

}
