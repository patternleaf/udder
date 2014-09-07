package com.coillighting.udder;

import com.coillighting.udder.TimePoint;

public class Frame {

	private TimePoint timePoint;
	private int value; // TEMP - eventually there will be a raster here, not an int

	public Frame(TimePoint timePoint, int value) {
		this.timePoint = timePoint;
		this.value = value;
	}

	public String toString() {
		return "Frame{time=" + this.timePoint + " value=" + this.value + "}";
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public TimePoint getTimePoint() {
		return this.timePoint;
	}
}
