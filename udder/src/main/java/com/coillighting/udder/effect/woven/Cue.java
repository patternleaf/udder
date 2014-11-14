package com.coillighting.udder.effect.woven;

import com.coillighting.udder.mix.Animator;

public interface Cue extends Animator {

    /** Rewind the cue's animation state so that it is ready to render anew
     *  from the beginning of the cue.
     */
    public void reset();

    public void setFadeState(CueFadeStateEnum fadeState);
    public CueFadeStateEnum getFadeState();

    public void setDuration(long duration);
    public long getDuration();

    /** Set the pixels that we'll be drawing on, then reset(). */
    public void setFrame(WovenFrame frame);
    public WovenFrame getFrame();

}
