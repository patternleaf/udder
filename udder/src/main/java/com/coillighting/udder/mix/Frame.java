package com.coillighting.udder.mix;

import com.coillighting.udder.model.Pixel;

/** An instantaneous sample of all timebased pixel values for the entire scene. */
public class Frame {

    private TimePoint timePoint;
    private Pixel[] pixels;

    /** Construct a new Frame by deeply copying the given Pixels so that it is
     * safe to give this Frame to a transmitter in another thread. (Per
     * Effect.render's contract, renderers are permitted to return direct
     * references to internal Pixels. This makes it safe.)
     *
     * timePoint is immutable, so it is safe to share by reference.
     */
    public static Frame createByCopy(TimePoint timePoint, Pixel[] otherPixels) {
        Frame frame = new Frame(timePoint, new Pixel[otherPixels.length]);
        for(int i=0; i<frame.pixels.length; i++) {
            frame.pixels[i] = new Pixel(otherPixels[i]);
        }
        return frame;
    }

    /** Construct a new Frame incorporating the given pixels by reference. */
    public Frame(TimePoint timePoint, Pixel[] pixels) {
        if(timePoint == null) {
            throw new NullPointerException("Frame requires a timePoint.");
        } else if(pixels == null) {
            throw new NullPointerException("Pixel array must not be null.");
        }
        this.timePoint = timePoint;
        this.pixels = pixels;
    }

    public String toString() {
        int len = 0;
        if(this.pixels != null) {
            len = this.pixels.length;
        }
        return "Frame{time=" + this.timePoint + ", " + len + " pixels}";
    }

    public void setPixels(Pixel[] pixels) {
        this.pixels = pixels;
    }

    public Pixel[] getPixels() {
        return this.pixels;
    }

    public TimePoint getTimePoint() {
        return this.timePoint;
    }
}
