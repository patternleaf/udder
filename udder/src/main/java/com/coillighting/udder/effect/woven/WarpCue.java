package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class WarpCue extends CueBase {

    protected int warpIndex = 0;

    protected long stepDuration = 0;
    protected long stepStartTime = 0;

    public WarpCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void startStepTimer(TimePoint timePoint) {
        stepStartTime = timePoint.sceneTimeMillis();
        stepDuration = (long)(this.getDuration() / (double)frame.warp.length);
    }

    public void animate(TimePoint timePoint) {
        if(fadeState == CueFadeStateEnum.START) {
            warpIndex = 0;
            this.startTimer(timePoint);
            this.startStepTimer(timePoint);

            for(Pixel p: frame.warp) {
                p.setColor(0.0f, 0.0f, 0.0f);
            }
        } else if(this.isElapsed(timePoint)) {
            // Finish this step, move on to the next cue.
            Pixel p = frame.warp[frame.warp.length - 1];
            p.setColor(1.0f, 1.0f, 1.0f); // TODO color selection
            this.stopTimer();
            return;
        } else {
            double elapsed = this.computeFractionElapsed(timePoint,
                stepStartTime, stepDuration);
            if(elapsed >= 1.0) {
                if(warpIndex + 1 >= frame.warp.length) {
                    // Already done with this cue.
                    return;
                } else {
                    // Finish this step, move to the next step.
                    Pixel p = frame.warp[warpIndex];
                    p.setColor(1.0f, 1.0f, 1.0f); // TODO color selection

                    warpIndex += 1;
                    this.startStepTimer(timePoint);
                    elapsed = 0.0;
                }
            }
            float f = (float) elapsed;
            Pixel p = frame.warp[warpIndex];
            // TODO nonlinear fade in
            p.setColor(f, f, f); // TODO color selection
        }
    }

}
