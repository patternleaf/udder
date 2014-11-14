package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.model.Pixel;
import com.coillighting.udder.model.RgbaArray;

public class ArrayEffect extends ArrayEffectBase {

    public ArrayEffect(RgbaArray wrappedPixels) {
        super(wrappedPixels);
    }

    public void setPixels(Integer [] rgbaPixels) {
        if(rgbaPixels == null) {
            throw new NullPointerException("An array of pixels is required.");
        }
        if(pixels != null) {
            for(int i=0; i<rgbaPixels.length && i<pixels.length; i++) {
                pixels[i].setRGBAColor(rgbaPixels[i]);
            }

            // If insufficient pixels were provided, black out the others.
            for(int i=rgbaPixels.length; i<pixels.length; i++) {
                pixels[i].setBlack();
            }
        }
    }

}
