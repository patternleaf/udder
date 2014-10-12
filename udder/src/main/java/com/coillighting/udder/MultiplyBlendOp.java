package com.coillighting.udder;

public class MultiplyBlendOp implements BlendOp {

	public float blend(float background, float foreground) {
		float val = background * foreground;
		if(val < 0.0) {
			val = 0.0;
		} else if(val > 1.0) {
			val = 1.0;
		}
		return val;
	}

}
