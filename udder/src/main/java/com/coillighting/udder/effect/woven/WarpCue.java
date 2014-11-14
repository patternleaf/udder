package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Util;
import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** Draw pixels representing the stringing of the warp in a loom. */
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
        this.threadColor = new Pixel(0.0f, 0.0f, 1.0f);
        this.cursorColor = Pixel.white();
        this.backgroundColor = Pixel.black();
        this.blendOp = new MaxBlendOp();
    }

    public void startStepTimer(TimePoint timePoint) {
        stepStartTime = timePoint.sceneTimeMillis();
        long steps = (1 + frame.warp.length) / 2;
        stepDuration = this.getDuration() / steps;
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

        } else if(fadeState == CueFadeStateEnum.RUNNING) {

            if(this.isElapsed(timePoint)) {
                // Finish the outgoing step as well as any remaining
                // steps, then move on to the next cue. Timing is never
                // perfect, so a few milliseconds are lost with each
                // step, so for extremely fast cue durations,
                // we need to draw the remainder before finishing.
                for(;warpIndex < frame.warp.length; warpIndex +=2) {
                    frame.warp[warpIndex].setColor(threadColor);
                }
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
                        frame.warp[warpIndex].setColor(threadColor);

                        // Skip every other column, so that it looks like a bunch of
                        // threads (lines) instead of a solid fill.
                        warpIndex += 2;
                        this.startStepTimer(timePoint);
                        return;
                    }
                }

                // Fade from white to threadColor as we fade in.
                Pixel color = new Pixel(threadColor); // could reuse this tmp obj
                float brightness = Util.reshapeExponential((float) elapsed, 2.0f);
                color.blendWith(cursorColor, 1.0f - brightness, blendOp);

                // Fade in from black
                color.scale(Util.reshapeExponential(brightness, 0.36f));

                frame.warp[warpIndex].setColor(color);

                float bgScale = 0.5f * (float) Util.reshapeExponential(
                    1.0 - this.getFractionElapsed(timePoint), 3.0);
                frame.background.setColor(bgScale * 1.0f, bgScale * 0.5f, bgScale * 0.0f);
            }
        }
    }

}
