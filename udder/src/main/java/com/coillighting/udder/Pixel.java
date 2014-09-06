package com.coillighting.udder;


/** A simple data structure for representing high-resolution RGB pixel data.
 *  We eventually mix down to 8 bit RGB, but we animate and mix in this high
 *  resolution space. We have this luxury because our fixtures are so few.
 */
public class Pixel {
	public double r=0.0;
	public double g=0.0;
	public double b=0.0;
}
