package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.RgbaRaster;

public abstract class ArrayEffectBase extends EffectBase {

    public ArrayEffectBase(RgbaRaster raster) {
        if(raster == null) {
            this.setPixels(new Pixel(0.0f, 0.0f, 0.0f));
        } else {
            Integer[] rgbaPixels = raster.getPixels();
            if(rgbaPixels == null) {
                this.setPixels(new Pixel(0.0f, 0.0f, 0.0f));
            } else {
                this.setPixels(rgbaPixels);
            }
        }
    }

    public Class getStateClass() {
        return RgbaRaster.class;
    }

    public Object getState() {
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        RgbaRaster raster = (RgbaRaster) state;
        this.setPixels(raster.getPixels());
    }

    public void setPixels(Pixel color) {
        if(color == null) {
            throw new NullPointerException("RasterEffect requires a color.");
        }
        if(pixels != null) {
            for(Pixel pixel: pixels) {
                pixel.setColor(color);
            }
        }
    }

    public abstract void setPixels(Integer [] rgbaPixels);

}
