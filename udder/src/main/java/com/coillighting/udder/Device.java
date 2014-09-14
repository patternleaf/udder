package com.coillighting.udder;


/** A logical Device such as a cluster of LEDs that are animated as a single
 *  pixel. See also notes in Patchable.java.
 */
public class Device extends Object {

	/** This Device's address in some arbitrary address space. For the dairy,
	 *  this address is in the space of a single OPC channel.
	 *  (TODO: double-check this assumption.)
	 */
	private long addr = 0;

	/** A dirt simple grouping mechanism. Each Device belongs to exactly one
	 *  group (for now). For the Dairy installation, this will indicate gate
	 *  0 or gate 1, in case an Animator cares which group the Device is in.
	 */
	private long group=0;

	/** Position in model space. */
	private double x = 0;
	private double y = 0;
	private double z = 0;

	public Device(long addr, long group, double x, double y, double z) {
		this.addr = addr;
		this.group = group;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return "Device @"+addr+" ["+group+"] ("+x+","+y+","+z+")";
	}

	// TODO refactor x, y and z as a double[] for consistency with other classes?
	public double[] getPoint() {
		return new double[]{x, y, z};
	}

}
