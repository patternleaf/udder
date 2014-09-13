package com.coillighting.udder;


import com.coillighting.udder.Device;

/** This class is used as a JSON schema spec and output datatype for the Boon
 *  JsonFactory when it deserializes JSON patch sheets exported from Eric's
 *  visualizer. This is just an intermediate representation. We immediately
 *  convert these PatchElements to Devices after parsing.
 */
public class PatchElement {

	private double[] point;
	private long gate; // a.k.a. group FIXME - ask Eric

	public PatchElement(double[] point, long gate) {
		this.point = point;
		this.gate = gate;
	}

	/** Given an address, conver this intermediate representation into a full
     *  fledged Udder Device. TODO: work out address mappings from model space
     *  to OPC low-level addr space.
     */
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
