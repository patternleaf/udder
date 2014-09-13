package com.coillighting.udder;


/** A frozen point in time against which all frame animations are calculated,
 *  in order to keep nonrandom effects repeatably and deterministically locked
 *  in synch with one another and aligned to a common timeline from one frame to
 *  the next.
 *
 *  Normally a TimePoint is constructed once per animation loop, before the
 *  loop executes, and passed to each call to Animator.animate().
 *
 *  For thread safety, this class is intended to be immutable after
 *  initialization.
 */
public class TimePoint {

	/** The nomimally "real" time at which this frame was initialized. Although
	 *  this time is required by some animations which need to synchronize their
	 *  effects with the outside world -- a dusk/dawn almanac scheduler, for
	 *  instance -- you should ordinarily never refer to the realTimeMillis in
	 *  your animation functions. Instead, use TimePoint.sceneTimeMillis in
	 *  order to give the user the opportunity to apply timebase distortions.
	 *  Initialized with System.currentTimeMillis().
	 *
	 *  Not to be confused with TimePoint.sceneTimeMillis.
	 */
	private long realTimeMillis = 0;

	/** The reference time for all elements of the current scene. By using this
	 *  offset as the current time for timebase calculations in your animators,
	 *  you allow the user to apply timebase distortions, a powerful creative
	 *  tool. For ideas of what you might do with timebase distortions, sit
	 *  down with an old Invisibl Skratch Piklz, then imagine stretching and
	 *  reversing your graphical animations as if they were audio, at high rez.
	 *  This time is compatible with, but not necessarily identical to, the
	 *  "real" millisecond time offset returned by System.currentTimeMillis().
	 *  When a show is playing back in nominally real time, with no timebase
	 *  transformations or distortions, then this value will by convention
	 *  equal TimePoint.realTimeMillis minus the realTimeMillis when the show
	 *  started, and then this value will count up monotonically thereafter.
	 *  However, there is no guarantee that the user will choose to follow such
	 *  a straightforward path through time.
     *
	 *  Not to be confused with TimePoint.realTimeMillis.
	 */
	private long sceneTimeMillis = 0;

	/** An integer counting the current frame. Normally a show begins at frame
	 *  0 and proceeds to count up, +1 per frame, but there is no guarantee that
	 *  a user will configure a show to count incrementally. There is even no
	 *  guarantee that frameIndex will monotonically increase. However, a normal
	 *  show which doesn't replay its own history will by convention simply
	 *  start from 0 and monotonically increment +1 per animation loop.
	 */
	private long frameIndex = 0;

	public TimePoint(long realTimeMillis, long sceneTimeMillis, long frameIndex) {
		this.realTimeMillis = realTimeMillis;
		this.sceneTimeMillis = sceneTimeMillis;
		this.frameIndex = frameIndex;
	}

	/** Copy constructor. */
	public TimePoint(TimePoint timePoint) {
		this.realTimeMillis = timePoint.realTimeMillis;
		this.sceneTimeMillis = timePoint.sceneTimeMillis;
		this.frameIndex = timePoint.frameIndex;
	}

	/** Initialize a TimePoint with the current system time as its nominally
	 *  "real" time offset. Start counting the scene time from 0 and frameIndex
	 *  at 0.
	 */
	public TimePoint() {
		this.realTimeMillis = System.currentTimeMillis();
		this.sceneTimeMillis = 0;
		this.frameIndex = 0;
	}

	/** Return a new immutable TimePoint, incrementing frameIndex and updating
	 *  realTimeMillis and sceneTimeMillis on the assumption that the show is
	 *  taking a straightforward and undistorted path through time.
	 */
	public TimePoint next() {
		long realTime = System.currentTimeMillis();
		long sceneTimeOffsetMillis = this.realTimeMillis - this.sceneTimeMillis;
		return new TimePoint(
			realTime,
			realTime - sceneTimeOffsetMillis,
			1 + this.frameIndex);
	}

	public long realTimeMillis() {
		return this.realTimeMillis;
	}

	public long sceneTimeMillis() {
		return this.sceneTimeMillis;
	}

	public long getFrameIndex() {
		return this.frameIndex;
	}

	public String toString() {
		return "" + this.sceneTimeMillis + '[' + this.frameIndex + ']';
	}

}
