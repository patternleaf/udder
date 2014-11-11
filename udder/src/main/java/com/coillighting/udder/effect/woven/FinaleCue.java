package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.Util;

public class FinaleCue extends CueBase {

    protected double timeShare;
    protected int approxPulseCount;
    protected static double offset = 1.2533141373155001;
    protected double alignment;
    protected double contrast;
    protected float brightness;

    public FinaleCue(long duration, WovenFrame frame) {
        super(duration, frame);

        // this much for accelerating pulses, saving the rest for the last hurrah
        timeShare = 0.8;
        approxPulseCount = 12; // keep this an even number for no glitches
        alignment = FinaleCue.computeAlignment(approxPulseCount);
        contrast = 0.75; // out of 1.0 max = 100% contrast
        brightness = 0.75f; // allow some warp and weft to shine through
    }

    protected static double computeAlignment(int pulseCount) {
        double c = 0.5 * (double) pulseCount;
        return FinaleCue.offset + Math.pow((2*Math.PI*c + Math.PI*0.5), 0.5);
    }

    /** See woven_scene.txt docs for details.
     * Wolfram alpha plots:
     * We start with this:
     * http://www.wolframalpha.com/input/?i=0.5+%2B+0.5+*+sin%28%28x+-+1.253314137%29**+2%29+from+0+to+11.9
     * And then we sort of rectify the sinusoidal waveform and tweak the contrast like this:
     * http://www.wolframalpha.com/input/?i=%281%2F3%29+%2B+%282%2F3%29+*+abs%28sin%28%28x+-+1.253314137%29**+3%29%29+from+0+to+11.9
     */
    public double accelerate(double x) {
        // convert from millis
        double duration_sec = (double) duration / 1000.0;

        // 5.772202523670451 == peak2a(3) for approx. 6 pulses -- see docs
        double timescale = duration_sec * timeShare / alignment;
        x /= timescale;
        return 1.0 - contrast + contrast * Math.abs(Math.sin(Math.pow(x - FinaleCue.offset, 2.0)));
    }

    // for 4 lumps (after the starting value, a half-peak at x=0.0), cycle
    // x in range [0...4.0*pi]. n pi => n lumps. usually x=elapsed seconds.
    public double plateau(double x) {
        double shape = 0.25; // also worth trying 1.0 and 0.5
        double y = Math.abs(Math.sin(x - 0.5 * Math.PI));
        return Math.pow(y, shape);
    }

    // x in range [0.0..1.0]
    public double fadeOut(double x) {
        return Util.reshapeExponential(1.0 - x, 1.1);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            this.frame.background.setBlack();

        } else if(this.fadeState == CueFadeStateEnum.RUNNING) {
            double elapsed = this.getFractionElapsed(timePoint);
            if(elapsed >= 1.0) {
                this.stopTimer();
            } else {
                long elapsedMillis = timePoint.sceneTimeMillis() - startTime;
                double elapsedSeconds = elapsedMillis / 1000.0;
                float y;

                // quick linear fade in to soften the blow
                double scale = brightness;
                if(elapsedSeconds < 1.0) scale *= elapsedSeconds;

                if (elapsed <= timeShare) {
                    // STEP A - accelerating oscillation
                    // TODO - involve all the colors in all the layers - maybe add
                    // a scaling factor to WovenFrame itself?
                    y = (float) this.accelerate(elapsedSeconds);
                } else {
                    double plateauDuration = (1.0 - timeShare) * 0.666;
                    double releaseDuration = 1.0 - timeShare - plateauDuration;

                    if(elapsed <= timeShare + plateauDuration) {
                        // STEP B - hold it
                        double x = (elapsed - timeShare) / plateauDuration;
                        y = (float) this.plateau(x);
                    } else {
                        // STEP C - sudden release envelope, tapering out at the end
                        double x = (elapsed - timeShare - plateauDuration) / releaseDuration;
                        y = (float) this.fadeOut(/*(elapsed - timeShare) / (1.0 - timeShare)*/x);
                        scale = 1.0; // savin' the best for last
                    }
                }
                // maybe separate backgrounds for front and rear gate?

                float bgScale = y * (float)scale;
                // TODO variable color here
                this.frame.background.setColor(bgScale * 0.20f, bgScale * 0.0f, bgScale * 1.0f);
            }
        }
    }

}
