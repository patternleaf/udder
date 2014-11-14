package com.coillighting.udder.effect.woven;

import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** All black, no fade. */
public class BlackoutCue extends CueBase {

    public BlackoutCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            frame.setColor(Pixel.black()); // only redraw once
        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            if(this.isElapsed(timePoint)) {
                this.stopTimer();
            }
        }
    }

}
