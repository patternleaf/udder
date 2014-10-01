package com.coillighting.udder;

public class MaxBlendOp implements BlendOp {

	public float blend(float background, float foreground) {
		return background >= foreground ? background : foreground;
	}

}
