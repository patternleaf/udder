package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.RgbaRaster;

// TODO rename -- the etymology of 'raster' is the latin word for 'rake',
// so a raster has really got to be a 2D datastructure.
public class RasterEffect extends ArrayEffectBase {

    public RasterEffect(RgbaRaster raster) {
        super(raster);
    }

    public void setPixels(Integer [] rgbaPixels) {
        if(rgbaPixels == null) {
            throw new NullPointerException("RasterEffect requires rgbaPixels.");
        }
        if(pixels != null) {
            for(int i=0; i<rgbaPixels.length && i<pixels.length; i++) {
                pixels[i].setRGBAColor(rgbaPixels[i]);
            }
        }
    }

}
