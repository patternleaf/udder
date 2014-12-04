package com.coillighting.udder.effect.woven;

import com.coillighting.udder.geometry.Reshape;
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

            // Bright fade out, quick tail-off:
            // http://www.wolframalpha.com/input/?i=%281.0-x%29**0.4+from+0+to+1
            this.frame.setBrightness(Reshape.exponential(1.0 - elapsed, 0.4));
        }
    }

}
