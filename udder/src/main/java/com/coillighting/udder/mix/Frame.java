package com.coillighting.udder.mix;

import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** An instantaneous sample of all timebased pixel values for the entire scene. */
public class Frame {

    private TimePoint timePoint;
    private Pixel[] pixels;

    public Frame(TimePoint timePoint, Pixel[] pixels) {
        if(timePoint == null) {
            throw new NullPointerException("Frame requires a timePoint.");
        }
        this.timePoint = timePoint;
        this.setPixels(pixels);
    }

    public String toString() {
        int len = 0;
        if(this.pixels != null) {
            len = this.pixels.length;
        }
        return "Frame{time=" + this.timePoint + ", " + len + " pixels}";
    }

    public void setPixels(Pixel[] pixels) {
        if(pixels == null) {
            throw new NullPointerException("Pixel array must not be null.");
        } else if(pixels.length < 2000) { // TEMP-DEBUG
            throw new NullPointerException("Corrupt Frame: pixel array len=" + pixels.length);
        }
        this.pixels = pixels;
    }

    public Pixel[] getPixels() {
        return this.pixels;
    }

    public TimePoint getTimePoint() {
        return this.timePoint;
    }
}
