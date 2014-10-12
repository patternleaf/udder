package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class BlackoutCue extends CueBase {

    public BlackoutCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState != CueFadeStateEnum.INVISIBLE) {
            // Placeholder: black out the scene
            this.frame.setColor(Pixel.black());
        }
    }

}
