package com.coillighting.udder.effect.woven;

import com.coillighting.udder.TimePoint;

public class CurtainCue extends CueBase {

    public CurtainCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
        } else {
            float f = (float) this.getFractionElapsed(timePoint);
            // TODO: color selection, nonlinear fade-in
            frame.background.setColor(f, f, f);
            if(f >= 1.0f) {
                this.stopTimer();
            }
        }
    }

}
