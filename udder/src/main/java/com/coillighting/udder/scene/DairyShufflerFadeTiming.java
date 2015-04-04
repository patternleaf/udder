package com.coillighting.udder.scene;

/**
 * A basic data structure for communicating per-cue timing info to a
 * DairyShuffler.
 *
 * ivars are simple public for fastest access in hot animation loops.
 */
public class DairyShufflerFadeTiming {

    /** range: 0.0 to 1.0 */
    public double in = 1.0;

    public double out = 1.0;

    public DairyShufflerFadeTiming(double in, double out) throws IllegalArgumentException {
        if(in < 0.0) {
            throw new IllegalArgumentException("Fade-in time must be at least 0.0, not " + in);
        } else if(in > 1.0) {
            throw new IllegalArgumentException("Fade-in time must be at most 1.0, not " + in);
        } else {
            this.in = in;
        }

        if(out < 0.0) {
            throw new IllegalArgumentException("Fade-out time must be at least 0.0, not " + out);
        } else if(out > 1.0) {
            throw new IllegalArgumentException("Fade-out time must be at most 1.0, not " + out);
        } else {
            this.out = out;
        }
    }
}

