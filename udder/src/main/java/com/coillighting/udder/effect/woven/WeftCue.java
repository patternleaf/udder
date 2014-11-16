package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Util;
import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** Draw pixels representing the weaving of the weft in a loom. */
public class WeftCue extends CueBase {

    protected int weftX = 0;
    protected int weftY = 0;

    protected long stepDuration = 0;
    protected long stepStartTime = 0;

    protected Pixel threadColor = null;
    protected Pixel cursorColor = null;
    protected Pixel backgroundColor = null;

    protected BlendOp blendOp = null;

    public WeftCue(long duration, WovenFrame frame, Pixel threadColor) {
        super(duration, frame);

        this.threadColor = new Pixel(threadColor);
        this.cursorColor = Pixel.white();
        this.backgroundColor = Pixel.black();
        this.blendOp = new MaxBlendOp();
    }

    public void startStepTimer(TimePoint timePoint) {
        stepStartTime = timePoint.sceneTimeMillis();

        // Just one step per row, zigzagging.
        stepDuration = (long)(this.getDuration() / (double)frame.weft[0].length);
    }

    public void animate(TimePoint timePoint) {
        if(fadeState == CueFadeStateEnum.START) {
            weftX = 0;
            weftY = 0;
            this.startTimer(timePoint);
            this.startStepTimer(timePoint);

            // Clear the canvas
            for(Pixel[] column: frame.weft) {
                for(Pixel p: column) {
                    p.setColor(backgroundColor);
                }
            }

        } else if(fadeState == CueFadeStateEnum.RUNNING) {

            if(this.isElapsed(timePoint)) {
                // Finish the outgoing step as well as any remaining
                // steps, then move on to the next cue. Timing is never
                // perfect, so a few milliseconds are lost with each
                // step, so for extremely fast cue durations,
                // we need to draw the remainder before finishing.
                for (; weftY < frame.weft[0].length; weftY++) {
                    frame.weft[weftX][weftY].setColor(threadColor);
                    weftX = (weftX == 0 ? 1 : 0);
                }
                this.stopTimer();
                return;
            } else {
                double elapsed = CueBase.computeFractionElapsed(timePoint,
                        stepStartTime, stepDuration);

                if (elapsed >= 1.0) {
                    if (weftY >= frame.weft[0].length) {
                        // Already done with this cue.
                        return;
                    }
                    // Finish this step, zigzag up to the next step.
                    Pixel p = frame.weft[weftX][weftY];
                    p.setColor(threadColor);

                    weftY += 1;

                    if (weftY >= frame.weft[0].length) {
                        // Done with this cue.
                        return;
                    }

                    weftX = (weftX == 0 ? 1 : 0);
                    this.startStepTimer(timePoint);
                    return;
                }

                // Fade from white to threadColor as we fade in.
                Pixel color = new Pixel(threadColor); // could reuse this tmp obj
                float brightness = Util.reshapeExponential((float) elapsed, 2.0f);
                color.blendWith(cursorColor, 1.0f - brightness, blendOp);

                // Fade in from black, a little jumpier than warp because the shuttle is snappy.
                color.scale(Util.reshapeExponential(brightness, 0.25f));

                frame.weft[weftX][weftY].setColor(color);
            }
        }
    }

    public Pixel getThreadColor() {
        return threadColor;
    }

    public void setThreadColor(Pixel threadColor) {
        this.threadColor = threadColor;
    }

}
