package com.coillighting.udder.scene;

import com.coillighting.udder.effect.woven.WovenEffect;
import com.coillighting.udder.mix.Animator;
import com.coillighting.udder.mix.Layer;
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
    int shuffleLayerStartIndex; // inclusive
    int shuffleLayerEndIndex; // inclusive
    long cueStartTimeMillis;
    long textureCueDurationMillis;
    long wovenCueDurationMillis;
    long cueDurationMillis;

    boolean wovenMode;
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
                throw new IllegalArgumentException(
                    "This mixer contains no layer at wovenLayerIndex="
                    + wovenLayerIndex);

            } else if(shuffleLayerStartIndex < 0 || shuffleLayerStartIndex >= ct) {
                throw new IllegalArgumentException(
                    "This mixer contains no layer at shuffleLayerStartIndex="
                    + shuffleLayerStartIndex);

            } else if(shuffleLayerEndIndex < 0 || shuffleLayerEndIndex >= ct) {
                throw new IllegalArgumentException(
                    "This mixer contains no layer at shuffleLayerEndIndex="
                    + shuffleLayerStartIndex);

            } else if(shuffleLayerEndIndex <= shuffleLayerStartIndex) {
                throw new IllegalArgumentException(
                    "A shuffler's shuffleLayerStartIndex may not exceed its shuffleLayerEndIndex.");

            } else if(shuffleLayerStartIndex <= wovenLayerIndex
                    && wovenLayerIndex <= shuffleLayerEndIndex) {
                throw new IllegalArgumentException(
                    "A shuffler's woven layer may not also be a shuffled layer.");
            }
        }
        this.mixer = mixer;
        this.wovenLayerIndex = wovenLayerIndex;
        this.shuffleLayerStartIndex = shuffleLayerStartIndex;
        this.shuffleLayerEndIndex = shuffleLayerEndIndex;
        textureCueDurationMillis = 1000; // TODO: user-adjustable step time
        this.reset();
        this.mixer.subscribeAnimator(this);
    }

    public void reset() throws ClassCastException {
        wovenMode = true;
        Layer wovenLayer = (Layer) mixer.getLayer(wovenLayerIndex);
        WovenEffect wovenEffect = (WovenEffect) wovenLayer.getEffect();
        wovenCueDurationMillis = wovenEffect.getDurationMillis();
        cueDurationMillis = wovenCueDurationMillis;
        incomingLayerIndex = -1; // < 0: nothing incoming
        incomingLevel = 0.0f;
        primaryLevel = 0.0f;
        outgoingLevel=0.0f;
        wovenLevel = 0.0f;
        cueStartTimeMillis = -1; // < 0: not started
    }

    // switch off woven vs. other layers only at the transition point,
    // so human operators can play around with transient looks,
    // like woven + textures
    public void animate(TimePoint timePoint) {
        // Step forward or rewind and start over if needed.
        long now = timePoint.sceneTimeMillis();
        if(cueStartTimeMillis < 0) {
            cueStartTimeMillis = now;
        }
        long end = cueStartTimeMillis + cueDurationMillis;
        if(end < now) {
            // Step forward to the next track in the playlist.
            if(wovenMode) {
                // Switch to texture mode
                cueDurationMillis = textureCueDurationMillis;
                wovenMode = false;
                wovenLevel = 0.0f;
                this.setLevelConditionally(wovenLevel, wovenLayerIndex);
                incomingLayerIndex = shuffleLayerStartIndex;
            } else if(incomingLayerIndex >= shuffleLayerEndIndex + 2) {
                // Switch to woven mode
                for(int i=shuffleLayerStartIndex; i<=shuffleLayerEndIndex; i++) {
                    this.setLevelConditionally(0.0f, i);
                }
                this.reset();
            } else {
                cueDurationMillis = textureCueDurationMillis;
                // finish fading out the outgoing track if needed:
                this.setLevelConditionally(0.0f, incomingLayerIndex - 2);
                outgoingLevel = primaryLevel;
                primaryLevel = incomingLevel;
                incomingLevel = 0.0f;
                incomingLayerIndex++;
            }
            cueStartTimeMillis = now;
        }

        if(wovenMode) {
            wovenLevel = 1.0f;
            this.setLevelConditionally(wovenLevel, wovenLayerIndex);
        } else { // texture mode
            // incoming look (if applicable)
            int li = incomingLayerIndex;
            incomingLevel = 0.6f;
            this.setLevelConditionally(incomingLevel, li);

            // primary look (if applicable)
            li -= 1;
            primaryLevel = 1.0f;
            this.setLevelConditionally(primaryLevel, li);

            // outgoing look (if applicable)
            li -= 1;
            outgoingLevel = 0.5f;
            this.setLevelConditionally(outgoingLevel, li);
        }

    }

    private final void setLevelConditionally(float level, int layerIndex) {
        if(layerIndex == wovenLayerIndex
                || (layerIndex >= shuffleLayerStartIndex
                    && layerIndex <= shuffleLayerEndIndex)) {
            mixer.getLayer(layerIndex).setLevel(level);
        }
    }

    public void log(Object msg) {
        System.out.println(msg);
    }
}
