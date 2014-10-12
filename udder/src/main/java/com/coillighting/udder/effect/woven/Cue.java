package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Animator;

public interface Cue extends Animator {

    public long getDuration();

    public void reset();

    public void setFrame(WovenFrame frame);

}
