package com.coillighting.udder;

import com.coillighting.udder.BlendOp;

public class MaxBlendOp implements BlendOp {

	public double blend(double background, double foreground) {
		return background >= foreground ? background : foreground;
	}

}
