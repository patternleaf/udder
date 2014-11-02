package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;
import com.coillighting.udder.Util;

public class CurtainCue extends CueBase {

    public CurtainCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            frame.setBrightness(0.0);
            frame.background.setColor(1.0f, 0.5f, 0.0f);
        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            double elapsed = this.getFractionElapsed(timePoint);
            if(elapsed >= 1.0) {
                this.stopTimer();
                frame.background.setColor(0.0f, 0.0f, 0.0f) ; // TEMP - TODO: blend with warp
            } else {
                // TODO: color selection, nonlinear fade-in. for now linear amber.
                frame.setBrightness(Util.reshapeExponential(elapsed,0.5));
            }
        }
    }

}
