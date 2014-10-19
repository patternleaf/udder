package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class FinaleCue extends CueBase {

    public FinaleCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);

            // Placeholder. TODO: some kind of animation. maybe woven sinewaves.
            this.frame.background.setColor(0.222f, 0.333f, 0.444f);
        } else if(this.isElapsed(timePoint)) {
            this.stopTimer();
        }
    }

}
