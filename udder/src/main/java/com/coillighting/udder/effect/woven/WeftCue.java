package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;

public class WeftCue extends CueBase {

    public WeftCue(long duration, WovenFrame frame) {
        super(duration, frame);
    }

    public void animate(TimePoint timePoint) {
        // Placeholder: light up the entire weft blue
        for(Pixel[] pixels: this.frame.weft) {
            for(Pixel p: pixels) {
                p.setColor(0.0f, 0.0f, 1.0f);
            }
        }
    }

}
