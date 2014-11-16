package com.coillighting.udder.scene;

import com.coillighting.udder.mix.Animator;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.mix.TimePoint;

/** Implements a 'shuffle' mode specifically tailored to the Boulder Dairy
 * Archway's scene. Cross-fades layers in groups that look good together,
 * periodically stopping to fade everything out and fade in the Woven effect,
 * with synchronized startup of the Woven effect from its opening queue
 * (a blackout of several seconds).
 *
 * When not displaying the Woven scene, shows 1 incoming look + 1 primary
 * look + 1 outgoing look. Fade-in of the incoming look and fade-out of
 * the outgoing look have their own easing curves, randomly selected from
 * the available modes.
 */
public class DairyShuffler implements Animator {

    protected Mixer mixer;
    int wovenLayerIndex;
    int shuffleLayerStartIndex;
    int shuffleLayerEndIndex;

    protected int incomingLayerIndex;
    protected float incomingLevel;
    protected float primaryLevel;
    protected float outgoingLevel;
    protected float wovenLevel;

    public DairyShuffler(Mixer mixer, int wovenLayerIndex, int shuffleLayerStartIndex, int shuffleLayerEndIndex) {
        if(mixer == null) {
            throw new NullPointerException("DairyShuffler requires a Mixer before it can shuffle.");
        } else {
            int ct = mixer.size();
            if(wovenLayerIndex < 0 || wovenLayerIndex >= ct) {
                throw new IllegalArgumentException("This mixer contains no layer at wovenLayerIndex=" + wovenLayerIndex);
            } else if(shuffleLayerStartIndex < 0 || shuffleLayerStartIndex >= ct) {
                throw new IllegalArgumentException("This mixer contains no layer at shuffleLayerStartIndex=" + shuffleLayerStartIndex);
            } else if(shuffleLayerEndIndex < 0 || shuffleLayerEndIndex >= ct) {
                throw new IllegalArgumentException("This mixer contains no layer at shuffleLayerEndIndex=" + shuffleLayerStartIndex);
            } else if(shuffleLayerEndIndex <= shuffleLayerStartIndex) {
                throw new IllegalArgumentException("A shuffler's shuffleLayerStartIndex may not exceed its shuffleLayerEndIndex.");
            } else if(shuffleLayerStartIndex <= wovenLayerIndex && wovenLayerIndex <= shuffleLayerEndIndex) {
                throw new IllegalArgumentException("A shuffler's woven layer may not also be a shuffled layer.");
            }
        }
        this.mixer = mixer;
        this.wovenLayerIndex = wovenLayerIndex;
        this.shuffleLayerStartIndex = shuffleLayerStartIndex;
        this.shuffleLayerEndIndex = shuffleLayerEndIndex;
        this.reset();
        this.mixer.subscribeAnimator(this);
    }

    public void reset() {
        incomingLayerIndex = -1;
        incomingLevel = 0.0f;
        primaryLevel = 0.0f;
        outgoingLevel=0.0f;
        wovenLevel = 0.0f;
    }

    public void animate(TimePoint timePoint) {
    }

    public void computeLevels(TimePoint timePoint) {

    }

    public void fadeLevels() {

    }
}
