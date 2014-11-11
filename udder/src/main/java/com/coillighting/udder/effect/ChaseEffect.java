package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.RgbaRaster;
import com.coillighting.udder.TimePoint;

public class ChaseEffect extends ArrayEffectBase {

    protected int offset = 0; // current scrolling offset
    protected int step = 1; // how fast to scroll
    protected Integer[] rgbaTexture = null;

    public ChaseEffect(RgbaRaster raster) {
        super(raster);
    }

    /** Draw a single frame, then scroll everything by one step. */
    public void animate(TimePoint timePoint) {
        this.setPixels(new Pixel(0.0f, 0.0f, 0.0f));
        if(rgbaTexture != null && pixels != null) {
            for(int t=0; t<rgbaTexture.length; t++) {
                int p = (t + offset) % pixels.length;
                pixels[p].setRGBAColor(rgbaTexture[t]);
            }
            offset += step;
        }
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getStep() {
        return this.step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setPixels(Integer [] rgbaTexture) {
        this.rgbaTexture = rgbaTexture;
    }

}
