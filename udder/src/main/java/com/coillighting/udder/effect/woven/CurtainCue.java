package com.coillighting.udder.effect.woven;

import com.coillighting.udder.geometry.Reshape;
import com.coillighting.udder.mix.TimePoint;

/** Bring up an unremarkable background color (e.g. amber wash). */
public class CurtainCue extends CueBase {

    public CurtainCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            frame.setBrightness(0.0);
            float bgScale = 0.5f;
            frame.background.setColor(bgScale * 1.0f, bgScale * 0.5f, bgScale * 0.0f);
        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            double elapsed = this.getFractionElapsed(timePoint);
            if(elapsed >= 1.0) {
                this.stopTimer();
            } else {
                frame.setBrightness(Reshape.exponential(elapsed,0.5));
            }
        }
    }

}
