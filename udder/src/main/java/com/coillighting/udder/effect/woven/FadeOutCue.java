package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class FadeOutCue extends CueBase {

    public FadeOutCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);

            // Placeholder. TODO: scale everything layer down to black by 100%-cue%
            this.frame.background.setColor(0.111f, 0.111f, 0.111f);
        } else {
            double elapsed = this.getFractionElapsed(timePoint);
            // TODO: color selection.
            // TODO: apply some shaping to this envelope, which currently will
            // accel as it tapers into black because it is compounding.
            frame.scaleColor(1.0 - elapsed);
            if(elapsed >= 1.0) {
                this.stopTimer();
            }
        }
    }

}
