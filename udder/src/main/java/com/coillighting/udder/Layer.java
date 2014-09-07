package com.coillighting.udder;

import java.lang.UnsupportedOperationException;

import com.coillighting.udder.Animator;
import com.coillighting.udder.MixableBase;
import com.coillighting.udder.Mixable;


public class Layer extends MixableBase implements Animator, Mixable {

	public void animate(TimePoint timePoint) {
		throw new UnsupportedOperationException("TODO - render this layer's effect state as rgb pixels");
	}

	public void mixWith(Pixel[] otherPixels) {
		throw new UnsupportedOperationException("TODO - mix r, g, b per pixel");
	}

}
