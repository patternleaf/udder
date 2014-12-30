package com.coillighting.udder.effect.woven;

import java.util.LinkedHashMap;

import com.coillighting.udder.effect.EffectBase;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

import static com.coillighting.udder.util.LogUtil.log;
import static com.coillighting.udder.effect.woven.CueEnum.*;

/** The Woven effect conveys the motions of manually weaving on a loom. This
 *  effect is mapped onto the Boulder Dairy's architectural-scale metal tapestry
 *  sculpture, and its details are designed to mesh nicely with that target.
 *
 *  This effect consists of several cues, each of which is basically a step
 *  sequence with some easing in of each step, followed by a brief finale.
 *
 *  Effect originally conceived by Becky Vanderslice. Storyboarding and detailed
 *  design by Becky and Coil Lighting staff.
 */
public class WovenEffect extends EffectBase {

    /** The cuesheet represents the cues that will be run the next time the
     *  effect reaches that state. If the current cue is edited, the cuesheet is
     *  changed, but not the running cue, in order to avoid corrupting or
     *  recomputing its state.
     */
    protected LinkedHashMap<CueEnum, Cue> cues = null;
    protected Cue currentCue = null;
    protected CueEnum currentStep = null;
    protected WovenFrame frame = null;
    protected boolean verbose = false;

    public WovenEffect() {
        cues = new LinkedHashMap<CueEnum, Cue>();
        frame = new WovenFrame();

        // Default colors for warp and weft cue. You may also change this
        // any time using an HTTP command. We might want to desaturate
        // these a little, but for now we'll stay high-sat because BV
        // reported that greens and yellows were looking good on the
        // metal sculpture.
        Pixel warpColor = new Pixel(1.0f, 0.4f, 1.0f);
        Pixel weftColor = new Pixel(1.0f, 0.75f, 0.0f);

        int speedup = 100; // for rapid debugging, set > 1

        cues.put(BLACKOUT, new BlackoutCue(100 / speedup, frame)); // keep it quick or museum staff get antsy about blackout
        // cues.put(CURTAIN, new CurtainCue(6000 / speedup, frame)); cut 12/4 per crew's consensus
        cues.put(WARP, new WarpCue(30000 / speedup, frame, warpColor));
        cues.put(WEFT, new WeftCue(45000 / speedup, frame, weftColor));
        // cues.put(FINALE, new FinaleCue(15000 / speedup, frame)); cut (ditto)
        cues.put(FADEOUT, new FadeOutCue(6000 / speedup, frame));

        this.reset();
    }

    public long getDurationMillis() {
        long duration = 0;
        for(Cue cue: cues.values()) {
            duration += cue.getDuration();
        }
        return duration;
    }

    /** Stop processing cues and clear their states. Also replace the internal
     *  graphics buffers and reinitialize them to black.
     */
    public void reset() {
        this.frame.reset();
        currentCue = null;
        currentStep = null;
        for(Cue cue: cues.values()) {
            cue.setFrame(this.frame); // also resets
        }
    }

    /** Jump directly to the specified step. If step is null, reset the scene. */
    protected void setStep(CueEnum step) {
        // To stop after a certain cue for debugging without freezing the show
        // with a breakpoint, use this technique:
        // if(currentStep == WEFT) return; // TEMP-DEBUG

        if(step == null) {
            if(this.verbose) log("\n\n============================ reset woven scene");
            this.reset();
        } else {
            if(this.verbose) log("\n---------------------------- woven cue " + step);
            Cue cue = cues.get(step);
            if(cue == null) {
                throw new NullPointerException("Invalid cue: " + step);
            } else {
                // TODO refactor, no need to keep track of this two different ways
                currentCue = cue;
                currentStep = step;
                cue.reset();
            }
        }
    }

    /** Stop processing the current cue (if any) and step forward to the next.
     *  When finished, reset the scene.
     */
    public void nextCue() {
        if(currentCue == null) {
            this.setStep(BLACKOUT);
        } else {
            boolean takeNext = false;
            for(CueEnum step: cues.keySet()) {
                if(takeNext) {
                    this.setStep(step);
                    return;
                } else if(step == currentStep) {
                    takeNext = true;
                }
            }
            this.setStep(null);
        }
    }

    public Class getStateClass() {
        return WovenEffectState.class;
    }

    public Object getState() {
        return new WovenEffectState(
            ((WarpCue) cues.get(WARP)).getThreadColor(),
            ((WeftCue) cues.get(WEFT)).getThreadColor());
    }

    public void setState(Object state) throws ClassCastException {
        WovenEffectState command = (WovenEffectState) state;
        Pixel warp = command.getWarpThreadColor();
        if(warp != null) {
            ((WarpCue) cues.get(WARP)).setThreadColor(warp);
        }
        Pixel weft = command.getWeftThreadColor();
        if(weft != null) {
            ((WeftCue) cues.get(WEFT)).setThreadColor(weft);
        }
    }

    public void animate(TimePoint timePoint) {
        if(currentCue == null) {
            this.nextCue();
        }
        currentCue.animate(timePoint);
        if(currentCue.getFadeState() == CueFadeStateEnum.END) {
            this.nextCue();
        }
        // TEMP: ASCII placeholder animation
        if(this.verbose) log(this.frame);
    }

    public Pixel[] render() {
        frame.render(pixels, devices);
        return pixels;
    }

    /** Rewind when the layer is turned off and on. */
    public void levelChanged(double oldLevel, double newLevel) {
        if(oldLevel == 0.0f && newLevel > 0.0f) {
            this.reset();
        }
    }
}
