package com.coillighting.udder.scene;

import com.coillighting.udder.effect.woven.WovenEffect;
import com.coillighting.udder.geometry.Interpolator;
import com.coillighting.udder.mix.StatefulAnimator;
import com.coillighting.udder.mix.Layer;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.mix.TimePoint;

import java.awt.geom.Point2D;

import static com.coillighting.udder.geometry.Interpolator.Interpolation;

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
public class DairyShuffler implements StatefulAnimator {

    protected Mixer mixer;
    protected Interpolator interpolator;

    int wovenLayerIndex;
    int shuffleLayerStartIndex; // inclusive
    int shuffleLayerEndIndex; // inclusive
    long cueStartTimeMillis;
    long textureCueDurationMillis;
    long wovenCueDurationMillis;
    long cueDurationMillis;

    boolean enabled;
    boolean wovenMode;
    protected Interpolation interpolationModeIncoming;
    protected Interpolation interpolationModeOutgoing;
    protected int incomingLayerIndex;
    protected float incomingLevel;
    protected float primaryLevel;
    protected float outgoingLevel;
    protected float wovenLevel;

    // temp variables we don't want to keep reallocating in every event
    private Point2D.Double off;
    private Point2D.Double current;
    private Point2D.Double on;

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

        // These interpolator curve settings favor a three-way mix
        // over a one-way mix.
        // 0.15: fade in quick, linger for a while and fade out quick
        // 2.5: relatively brighter, more gradual fade in and out in POWER mode
        this.interpolator = new Interpolator(0.15, 2.5);
        this.mixer = mixer;
        this.wovenLayerIndex = wovenLayerIndex;
        this.shuffleLayerStartIndex = shuffleLayerStartIndex;
        this.shuffleLayerEndIndex = shuffleLayerEndIndex;
        textureCueDurationMillis = 60000; // TODO: user-adjustable step time, always > 0
        this.reset();
        off = new Point2D.Double(0.0, 0.0);
        current = new Point2D.Double(0.0, 0.0);
        on = new Point2D.Double(1.0, 1.0);
        enabled = true;
        this.mixer.subscribeAnimator(this);
    }

    public void reset() throws ClassCastException {
        wovenMode = true;
        Layer wovenLayer = (Layer) mixer.getLayer(wovenLayerIndex);
        WovenEffect wovenEffect = (WovenEffect) wovenLayer.getEffect();
        wovenCueDurationMillis = wovenEffect.getDurationMillis();
        wovenEffect.reset();
        cueDurationMillis = wovenCueDurationMillis;
        incomingLayerIndex = -1; // < 0: nothing incoming
        incomingLevel = 0.0f;
        primaryLevel = 0.0f;
        outgoingLevel=0.0f;
        wovenLevel = 0.0f;
        interpolationModeIncoming = Interpolation.SINUSOIDAL;
        interpolationModeOutgoing = Interpolation.SINUSOIDAL;
        cueStartTimeMillis = -1; // < 0: not started
    }

    // switch off woven vs. other layers only at the transition point,
    // so human operators can play around with transient looks,
    // like woven + textures
    public void animate(TimePoint timePoint) {
        if(enabled) {
            // Step forward or rewind and start over if needed.
            long now = timePoint.sceneTimeMillis();
            if (cueStartTimeMillis < 0) {
                cueStartTimeMillis = now;
            }
            long end = cueStartTimeMillis + cueDurationMillis;
            if (end < now) {
                // Step forward to the next track in the playlist.
                if (wovenMode) {
                    // Switch from woven to texture mode
                    cueDurationMillis = textureCueDurationMillis;
                    wovenMode = false;
                    wovenLevel = 0.0f;
                    mixer.getLayer(wovenLayerIndex).setLevel(wovenLevel);
                    incomingLayerIndex = shuffleLayerStartIndex;
                    interpolationModeIncoming = Interpolation.ROOT; // fade in quick, don't leave it black
                } else if (incomingLayerIndex >= shuffleLayerEndIndex + 2) {
                    // Switch to woven mode
                    for (int i = shuffleLayerStartIndex; i <= shuffleLayerEndIndex; i++) {
                        this.setTextureLevelConditionally(0.0f, i);
                    }
                    this.reset();
                    // level will be set below on transition into Woven effect
                } else {
                    // Start fading in the next texture.
                    // Choose a new easing curve each time.
                    interpolationModeIncoming = interpolator.randomMode(10, 40, 70);
                    interpolationModeOutgoing = interpolator.randomMode(10, 40, 70);

                    cueDurationMillis = textureCueDurationMillis;
                    // finish fading out the outgoing track if needed:
                    this.setTextureLevelConditionally(0.0f, incomingLayerIndex - 2);
                    outgoingLevel = primaryLevel;
                    primaryLevel = incomingLevel;
                    incomingLevel = 0.0f;
                    incomingLayerIndex++;
                }
                cueStartTimeMillis = now;
                end = cueStartTimeMillis + cueDurationMillis;
            }

            if (wovenMode) {
                if (cueStartTimeMillis == now) {
                    wovenLevel = 1.0f;
                    mixer.getLayer(wovenLayerIndex).setLevel(wovenLevel);
                }
            } else {
                double pct = (double) (now - cueStartTimeMillis) / cueDurationMillis;
                // incoming look (if applicable)
                int li = incomingLayerIndex;
                interpolator.interpolate2D(interpolationModeIncoming, pct, off, current, on);
                // FUTURE: implement a 1D Interpolator API, for now just piggyback
                // on the existing 2D API and ignore y.
                // FIXME: convert all layer levels to doubles.
                incomingLevel = (float) current.x;
                this.setTextureLevelConditionally(incomingLevel, li);

                // primary look (if applicable)
                li -= 1;
                primaryLevel = 1.0f;
                this.setTextureLevelConditionally(primaryLevel, li);

                // outgoing look (if applicable)
                li -= 1;
                interpolator.interpolate2D(interpolationModeOutgoing, pct, on, current, off);
                outgoingLevel = (float) current.x;
                this.setTextureLevelConditionally(outgoingLevel, li);
            }
        }
    }

    private void setTextureLevelConditionally(float level, int layerIndex) {
        if(layerIndex >= shuffleLayerStartIndex
                && layerIndex <= shuffleLayerEndIndex) {
            mixer.getLayer(layerIndex).setLevel(level);
        }
    }

    public Class getStateClass() {
        return DairyShufflerState.class;
    }

    public Object getState() {
        return new DairyShufflerState(enabled, textureCueDurationMillis);
    }

    public void setState(Object state) throws ClassCastException {
        DairyShufflerState command = (DairyShufflerState) state;
        this.enabled = command.getEnabled();
        long millis = command.getCueDurationMillis();
        if(millis > 0) {
            this.textureCueDurationMillis = millis;
        }
    }

    public void log(Object msg) {
        System.out.println(msg);
    }
}
