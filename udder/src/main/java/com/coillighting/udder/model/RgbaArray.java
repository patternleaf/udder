package com.coillighting.udder.model;

/** A wrapper to satisfy Boon's JSON mapper, which didn't like plain old
 *  Integer[].
 */
public class RgbaArray {

    private Integer[] pixels = null;

    public RgbaArray(Integer[] pixels) {
        this.pixels = pixels;
    }

    public Integer[] getPixels() {
        return this.pixels;
    }

}
