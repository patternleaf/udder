package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CueBase implements Cue {

    protected CueFadeStateEnum fadeState = null;

    /** Target duration in milliseconds. */
    protected long duration = 0;
    protected long startTime = 0;

    /** Rasters we might want to draw on for this cue. */
    protected WovenFrame frame = null;

    public CueBase(long duration, WovenFrame frame) {
        this.setDuration(duration);
        this.setFrame(frame);
    }

    public void setFadeState(CueFadeStateEnum fadeState) {
        this.fadeState = fadeState;
    }

    public CueFadeStateEnum getFadeState() {
        return fadeState;
    }

    public void setDuration(long duration) {
        if(duration <= 0) {
            this.duration = 0;
        } else {
            this.duration = duration;
        }
    }

    public long getDuration() {
        return this.duration;
    }

    public static double computeFractionElapsed(TimePoint timePoint,
            long startTime, long duration) {
        long time = timePoint.sceneTimeMillis();
        long end = startTime + duration;
        if(time >= end || duration <= 0) {
            return 1.0;
        } else if(time <= startTime) {
            return 0.0;
        } else {
            return (time - startTime) / (double) duration;
        }
    }

    /** Return [0.0... 1.0] for 0% to 100% elapsed at the given time. */
    public double getFractionElapsed(TimePoint timePoint) {
        return CueBase.computeFractionElapsed(timePoint, this.startTime,
            this.duration);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void startTimer(TimePoint timePoint) {
        this.startTime = timePoint.sceneTimeMillis();
        this.fadeState = CueFadeStateEnum.RUNNING;
    }

    public boolean isElapsed(TimePoint timePoint) {
        return this.getFractionElapsed(timePoint) >= 1.0;
    }

    public void stopTimer() {
        this.fadeState = CueFadeStateEnum.END;
    }

    public void setFrame(WovenFrame frame) {
        this.frame = frame;
        this.reset();
    }

    public WovenFrame getFrame() {
        return frame;
    }

    public void reset() {
        this.setFadeState(CueFadeStateEnum.START);
    }

    public void animate(TimePoint timePoint) {}

}
