package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CurtainCue extends CueBase {

    public CurtainCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState != CueFadeStateEnum.INVISIBLE) {
            // Bring up the lights a little so that it's not totally black.
            // TODO: decide on the color. currently red.
            this.frame.background.setColor(1.0f, 0.0f, 0.0f);
        }
    }

}
