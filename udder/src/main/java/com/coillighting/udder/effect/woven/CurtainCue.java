package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CurtainCue extends CueBase {

    public CurtainCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);

        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            float f = (float) this.getFractionElapsed(timePoint);
            if(f >= 1.0f) {
                this.stopTimer();
                frame.background.setColor(0.0f, 0.0f, 0.0f) ; // TEMP - TODO: blend with warp
            } else {
                // TODO: color selection, nonlinear fade-in. for now linear amber.
                frame.background.setColor(1.0f*f, 0.5f*f, 0.0f);
            }
        }
    }

}
