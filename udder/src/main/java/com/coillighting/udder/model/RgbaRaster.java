package com.coillighting.udder.model;

/** A wrapper to satisfy Boon's JSON mapper, which didn't like plain old
 *  Integer[].
 */
public class RgbaRaster {

    private Integer[] pixels = null;

    public RgbaRaster(Integer[] pixels) {
        // TODO assert not null
        this.pixels = pixels;
    }

    public Integer[] getPixels() {
        return this.pixels;
    }

}
