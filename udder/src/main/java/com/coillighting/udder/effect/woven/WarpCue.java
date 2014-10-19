package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;

public class WarpCue extends CueBase {

    protected int warpIndex = 0;

    protected long stepDuration = 0;
    protected long stepStartTime = 0;

    protected Pixel threadColor = null;
    protected Pixel cursorColor = null;
    protected Pixel backgroundColor = null;

    protected BlendOp blendOp = null;

    public WarpCue(long duration, WovenFrame frame) {
        super(duration, frame);

        // TODO variable colors
        this.threadColor = new Pixel(0.0f, 0.00f, 1.0f);
        this.cursorColor = Pixel.white();
        this.backgroundColor = Pixel.black();
        this.blendOp = new MaxBlendOp();
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

            // Clear the canvas
            for(Pixel p: frame.warp) {
                p.setColor(backgroundColor);
            }
        } else if(this.isElapsed(timePoint)) {
            // Finish this step, move on to the next cue.
            Pixel p = frame.warp[frame.warp.length - 1];
            p.setColor(threadColor);
            this.stopTimer();
            return;
        } else {
            double elapsed = CueBase.computeFractionElapsed(timePoint,
                stepStartTime, stepDuration);
            if(elapsed >= 1.0) {
                if(warpIndex + 1 >= frame.warp.length) {
                    // Already done with this cue.
                    return;
                } else {
                    // Finish this step, move to the next step.
                    Pixel p = frame.warp[warpIndex];
                    p.setColor(threadColor);

                    // TODO: blank lines between threads
                    // TODO: variable ratio of thread width to blank line width?
                    warpIndex += 1;
                    this.startStepTimer(timePoint);
                    elapsed = 0.0;
                }
            }
            // TODO nonlinear fade-in, poss. nonlinear cursor fade
            float brightness = (float) elapsed;

            // TODO reuse objects?
            Pixel color = new Pixel(threadColor);

            // Fade from white to threadColor as we fade in.
            color.blendWith(cursorColor, 1.0f - brightness, blendOp);

            // Fade in from black
            color.scale(brightness);

            frame.warp[warpIndex].setColor(color);
        }
    }

}
