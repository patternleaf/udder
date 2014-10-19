package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.Util;

public class FinaleCue extends CueBase {

    public FinaleCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public double accelerate(double x) {
        // see woven_scene.txt docs
        double y = 0.5 + 0.5 * Math.sin(Math.pow(x - 1.2533141373155001, 2));
        System.err.println(Util.plot1D(y));
        return y;
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState == CueFadeStateEnum.START) {
            this.startTimer(timePoint);
            // Placeholder. TODO: some kind of animation. maybe woven sinewaves.
            this.frame.background.setColor(0.222f, 0.333f, 0.444f);
        } else if(this.isElapsed(timePoint)) {
            this.stopTimer();
        }
        // TODO - involve all the colors in all the layers - maybe add
        // a scaling factor to WovenFrame itself?
        long elapsedMillis = timePoint.sceneTimeMillis() - startTime;
        double elapsedSeconds = elapsedMillis / 1000.0;
        float y = (float) this.accelerate(elapsedSeconds / 2.0);
        this.frame.background.setColor(y, y, y); // TEMP. TODO - scale instead
        // TODO fit number of peaks to the scheduled cue duration
    }

}
