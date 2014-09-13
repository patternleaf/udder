package com.coillighting.udder;


import com.coillighting.udder.Device;

public class PatchElement {

	private double[] point;
	private long gate; // a.k.a. group FIXME - ask Eric

	public PatchElement(double[] point, long gate) {
		this.point = point;
		this.gate = gate;
	}

	public Device toDevice(long addr) {
		return new Device(addr, this.gate, this.point[0], this.point[1], this.point[2]);
	}

	public String toString() {
		return "PatchElement(["+this.point[0]+","+this.point[1]+","+this.point[2]+"], "+gate+")";
	}

	public double[] getPoint() {
		return this.point;
	}

	public long getGate() {
		return this.gate;
	}

}
