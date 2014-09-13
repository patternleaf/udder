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

	private long x = 0;
	private long y = 0;
	private long z = 0;

	public void Device(long addr, long x, long y, long z) {
		this.addr = addr;

		this.x = x;
		this.y = y;
		this.z = z;
	}

}
