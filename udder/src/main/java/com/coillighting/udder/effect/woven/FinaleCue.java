package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class FinaleCue extends CueBase {

    public FinaleCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        // Placeholder: light up the entire warp a cold grey
        for(Pixel p: this.frame.warp) {
            p.setColor(0.77f, 0.88f, 0.99f);
        }

        // Placeholder: light up the entire weft a warm grey
        for(Pixel[] pixels: this.frame.weft) {
            for(Pixel p: pixels) {
                p.setColor(0.66f, 0.55f, 0.44f);
            }
        }
    }

}
