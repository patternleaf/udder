package com.coillighting.udder.effect.woven;

import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** Fade smoothly to black. */
public class FadeOutCue extends CueBase {

    public FadeOutCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            this.frame.setBrightness(1.0);

        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            double elapsed = this.getFractionElapsed(timePoint);
            if(elapsed >= 1.0) {
                this.frame.setBrightness(0.0);
                this.stopTimer();
            }
            this.frame.setBrightness(1.0 - elapsed);
        }
    }

}
