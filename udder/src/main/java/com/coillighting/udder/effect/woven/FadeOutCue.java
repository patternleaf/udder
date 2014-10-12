package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class FadeOutCue extends CueBase {

    public FadeOutCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        if(this.fadeState != CueFadeStateEnum.INVISIBLE) {
            // Placeholder: light up the entire warp a cold grey
            for(Pixel p: this.frame.warp) {
                p.setColor(0.44f, 0.55f, 0.66f);
            }

            // Placeholder: light up the entire weft a warm grey
            for(Pixel[] pixels: this.frame.weft) {
                for(Pixel p: pixels) {
                    p.setColor(0.33f, 0.22f, 0.11f);
                }
            }
        }
    }

}
