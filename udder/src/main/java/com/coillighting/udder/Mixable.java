package com.coillighting.udder;

import com.coillighting.udder.BlendOp;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public interface Mixable {

	public void animate(TimePoint timePoint);
	public void mixWith(Pixel[] otherPixels);

	public BlendOp getBlendOp();
	public void setBlendOp(BlendOp blendOp);

	public double getLevel();
	public void setLevel(double level);

}
