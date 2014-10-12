package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CueBase implements Cue {

    /** Target duration in milliseconds. */
    protected long duration = 0;

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

    /** Rewind the cue's animation state so that it is ready to render anew
     *  from the beginning of the cue.
     */
    public void reset() {}

    /** Set the pixels that we'll be drawing on, then reset(). */
    public void setFrame(WovenFrame frame) {
        this.frame = frame;
    }

    /** Set the pixels that we'll be drawing on, then reset(). */
    public WovenFrame getFrame() {
        return frame;
    }

    public void animate(TimePoint timePoint) {}

}
