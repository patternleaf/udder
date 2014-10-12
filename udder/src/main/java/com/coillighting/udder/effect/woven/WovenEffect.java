package com.coillighting.udder.effect.woven;

import java.util.LinkedHashMap;

import com.coillighting.udder.effect.EffectBase;
import static com.coillighting.udder.effect.woven.CueEnum.*;

public class WovenEffect extends EffectBase {

    /** The cuesheet represents the cues that will be run the next time the
     *  effect reaches that state. If the current cue is edited, the cuesheet is
     *  changed, but not the running cue, in order to avoid corrupting or
     *  recomputing its state.
     */
    protected LinkedHashMap<CueEnum, Cue> cues = null;
    protected Cue currentCue = null;
    protected CueEnum currentStep = null;

    public WovenScene() {
        cues = new LinkedHashMap<CueEnum, Cue>();
        cues.put(BLACKOUT, new BlackoutCue(1000));
        cues.put(CURTAIN, new CurtainCue(1000));
        cues.put(WARP, new WarpCue(2000));
        cues.put(WEFT, new WeftCue(3000));
        cues.put(FINALE, new FinaleCue(2000));
        cues.put(FADEOUT, new FadeOutCue(1000));
        this.reset();
    }

    /** Stop processing cues and clear their states. */
    public void reset() {
        currentCue = null;
        currentStep = null;
        for(Cue cue: cues.values()) {
            cue.reset();
        }
    }

    /** Jump directly to the specified step. If step is null, reset the scene. */
    protected void setStep(CueEnum step) {
        if(step == null) {
            this.reset();
        } else {
            Cue cue = cues.get(step);
            if(cue == null) {
                throw new NullPointerException("Invalid cue: " + step);
            } else {
                currentCue = cue;
                currentStep = step;
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
            for(CueEnum step: cues.keySet()) { // TODO confirm this is iterating in order as advertised
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
        return null; // TODO
    }

    public Object getState() {
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        // TODO
    }
}
