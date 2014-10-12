package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class WarpCue extends CueBase {

    public WarpCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState != CueFadeStateEnum.INVISIBLE) {
            // Placeholder: light up the entire warp green
            for(Pixel p: this.frame.warp) {
                p.setColor(0.0f, 1.0f, 0.0f);
            }
        }
    }

}
