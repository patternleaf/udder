package com.coillighting.udder.scene.woven;

public class CueBase implements Cue {

    /** Target duration in milliseconds. */
    protected long duration = 0;

    public CueBase(long duration) {
        if(duration <= 0) {
            this.duration = 0;
        } else {
            this.duration = duration;
        }
    }

    public long getDuration() {
        return this.duration;
    }

    public void reset() {}

}
