package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class WeftCue extends CueBase {

    protected int weftX = 0;
    protected int weftY = 0;

    protected long stepDuration = 0;
    protected long stepStartTime = 0;

    public WeftCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void startStepTimer(TimePoint timePoint) {
        stepStartTime = timePoint.sceneTimeMillis();
        stepDuration = (long)(this.getDuration()
            / (2.0 * (double)frame.weft[0].length));
    }

    public void animate(TimePoint timePoint) {
        if(fadeState == CueFadeStateEnum.START) {
            weftX = 0;
            weftY = 0;
            this.startTimer(timePoint);
            this.startStepTimer(timePoint);

            // Placeholder: light up the entire weft blue
            for(int x=0; x<2; x++) {
                for(Pixel p: frame.weft[x]) {
                    p.setColor(0.0f, 0.0f, 1.0f);
                }
            }
        } else if(this.isElapsed(timePoint)) {
            // Finish this step, move on to the next cue.
            Pixel p = frame.weft[1][frame.weft[1].length - 1];
            p.setColor(1.0f, 1.0f, 1.0f); // TODO color selection
            this.stopTimer();
            return;
        }

        double elapsed = this.computeFractionElapsed(timePoint,
            stepStartTime, stepDuration);
        if(elapsed >= 1.0) {
            if(weftX == 0) {
                // Finish this step, move right to the next step.
                Pixel p = frame.weft[weftX][weftY];
                p.setColor(1.0f, 1.0f, 1.0f); // TODO color selection
                weftX = 1;
            } else if(weftY + 1 >= frame.weft[0].length) {
                return;
            } else {
                // Finish this step, move up to the next step.
                Pixel p = frame.weft[weftX][weftY];
                p.setColor(1.0f, 1.0f, 1.0f); // TODO color selection

                weftY += 1;
                weftX = 0;
            }
            this.startStepTimer(timePoint);
        }

        float f = (float) elapsed;
        Pixel p = frame.weft[weftX][weftY];
        // TODO nonlinear fade in
        p.setColor(f, f, f); // TODO color selection
    }

}
