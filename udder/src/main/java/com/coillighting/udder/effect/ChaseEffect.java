package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.RgbaRaster;
import com.coillighting.udder.TimePoint;

public class ChaseEffect extends ArrayEffectBase {

    protected int offset = 0;

    public ChaseEffect(RgbaRaster raster) {
        super(raster);
    }

    public void animate(TimePoint timePoint) {
        ++this.offset;
    }

    public void setPixels(Integer [] rgbaPixels) {
        if(rgbaPixels == null) {
            throw new NullPointerException("RasterEffect requires rgbaPixels.");
        }
        if(pixels != null) {
            for(int i=0; i<rgbaPixels.length; i++) {
                pixels[(i + offset) % pixels.length].setColor(rgbaPixels[i]);
            }
        }
    }

}
