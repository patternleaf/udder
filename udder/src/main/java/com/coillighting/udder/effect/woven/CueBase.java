package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CueBase implements Cue {

    /** Target duration in milliseconds. */
    protected long duration = 0;

    /** Rasters we might want to draw on for this cue. */
    protected WovenFrame frame = null;

    public CueBase(long duration, WovenFrame frame) {
        this.setDuration(duration);
        this.setFrame(frame);
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

    public void setFrame(WovenFrame frame) {
        this.frame = frame;
    }

    public WovenFrame getFrame() {
        return frame;
    }

    public void reset() {}

    public void animate(TimePoint timePoint) {}

}
