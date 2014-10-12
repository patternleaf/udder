package com.coillighting.udder.filter;


public interface ContinuousLinearFilter {

	/** Given any double value, return some double value. This patchpoint
	 *  interface let you wire up scalings, inversions, dithering, distortions,
	 *  offsets, or anything else you might imagine.
 	 */
	public long filter(long x);

}
