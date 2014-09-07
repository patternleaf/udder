package com.coillighting.udder;

import com.coillighting.udder.Effect;


public class MonochromeEffect implements Effect {

	public void animate(TimePoint timePoint) {
		System.err.println("TODO - animate (solid color)");
	}

	public Pixel[] render() {
		System.err.println("TODO - render (solid color");
		return new Pixel[0]; // TEMP
	}
}
