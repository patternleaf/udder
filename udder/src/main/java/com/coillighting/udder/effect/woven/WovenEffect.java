package com.coillighting.udder.effect.woven;

import java.util.LinkedHashMap;

import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.effect.EffectBase;
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

    protected int width = 7;
    protected int height = 7;

    /** A single pixel maps onto the whole background. */
    protected Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
    protected Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
    protected Pixel[][] weft = null; // [x][y]

    public WovenEffect() {
        cues = new LinkedHashMap<CueEnum, Cue>();
        cues.put(BLACKOUT, new BlackoutCue(1000));
        cues.put(CURTAIN, new CurtainCue(1000));
        cues.put(WARP, new WarpCue(2000));
        cues.put(WEFT, new WeftCue(3000));
        cues.put(FINALE, new FinaleCue(2000));
        cues.put(FADEOUT, new FadeOutCue(1000));
        this.reset();
    }

    /** Stop processing cues and clear their states. Also replace the internal
     *  graphics buffers and reinitialize them to black.
     */
    public void reset() {
        currentCue = null;
        currentStep = null;
        for(Cue cue: cues.values()) {
            cue.reset();
        }

        background = Pixel.black();

        warp = new Pixel[width];
        for(int x=0; x<warp.length; x++) {
            warp[x] = Pixel.black();
        }

        weft = new Pixel[2][height];
        for(int x=0; x<weft.length; x++) {
            for(int y=0; y<weft[x].length; y++) {
                weft[x][y] = Pixel.black();
            }
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
        return Object.class; // TODO
    }

    public Object getState() {
        return null; // TODO
    }

    public void setState(Object state) throws ClassCastException {
        // TODO
    }

    protected void log(Object msg) {
        System.err.println(msg);
    }

    // TEMP: ASCII placeholder animation
    public void animate(TimePoint timePoint) {
        this.log("background " + background.toRGB() + " = " + background.toRGBA()); //TEMP-TEST

        StringBuffer warpsb = new StringBuffer("warp       ");
        for(int x=0; x<warp.length; x++) {
            warpsb.append(warp[x].toRGB()).append(' ');
        }
        this.log(warpsb);

        StringBuffer weftsb = new StringBuffer("weft       ");
        for(int y=0; y<weft[0].length; y++) {
            if(y > 0) {
                weftsb.append("\n           ");
            }
            for(int x=0; x<weft.length; x++) {
                weftsb.append(weft[x][y].toRGB()).append(' ');
            }
        }
        this.log(weftsb);
    }

}
