package com.coillighting.udder;


public class Frame {

	private int value; // TEMP

	public Frame(int value) {
		this.value = value;
	}

	public String toString() {
		return "Frame{value=" + this.value + "}";
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
