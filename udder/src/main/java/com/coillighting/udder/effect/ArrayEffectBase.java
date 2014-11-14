package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.model.Pixel;
import com.coillighting.udder.model.RgbaArray;

public abstract class ArrayEffectBase extends EffectBase {

    public ArrayEffectBase(RgbaArray wrappedPixels) {
        if(wrappedPixels == null) {
            this.setPixels(new Pixel(0.0f, 0.0f, 0.0f));
        } else {
            Integer[] pixels = wrappedPixels.getPixels();
            if(pixels == null) {
                this.setPixels(new Pixel(0.0f, 0.0f, 0.0f));
            } else {
                this.setPixels(pixels);
            }
        }
    }

    public Class getStateClass() {
        return RgbaArray.class;
    }

    public Object getState() {
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        RgbaArray wrappedPixels = (RgbaArray) state;
        this.setPixels(wrappedPixels.getPixels());
    }

    /** Set all pixels to the same color. */
    public void setPixels(Pixel color) {
        if(color == null) {
            throw new NullPointerException("A color is required.");
        }
        if(pixels != null) {
            for(Pixel pixel: pixels) {
                pixel.setColor(color);
            }
        }
    }

    /** Set many pixels to different colors. The specific mapping is up to the
     *  concrete subclass.
     */
    public abstract void setPixels(Integer [] rgbaPixels);

}
