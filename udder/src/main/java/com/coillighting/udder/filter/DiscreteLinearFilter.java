package com.coillighting.udder.filter;


public interface DiscreteLinearFilter {

	/** Given any long value, return some long value. This patchpoint interface
 	 *  let you wire up scalings, inversions, dithering, distortions, offsets,
 	 *  or anything else you might imagine.
 	 */
	public long filter(long x);

}
