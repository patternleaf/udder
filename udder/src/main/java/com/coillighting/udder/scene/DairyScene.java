package com.coillighting.udder.scene;

import java.util.ArrayList;
import java.util.Collection;
import static java.io.File.separator;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.blend.MultiplyBlendOp;
import com.coillighting.udder.effect.MonochromeEffect;
import com.coillighting.udder.effect.ChaseEffect;
import com.coillighting.udder.effect.ArrayEffect;
import com.coillighting.udder.effect.BloomEffect;
import com.coillighting.udder.effect.BloomEffectState;
import com.coillighting.udder.effect.Effect;
import com.coillighting.udder.effect.ImageEffect;
import com.coillighting.udder.effect.RollEffect;
import com.coillighting.udder.effect.TextureEffect;
import com.coillighting.udder.effect.woven.WovenEffect;
import com.coillighting.udder.mix.Layer;
import com.coillighting.udder.mix.Mixable;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** Define the scenegraph for the April, 2015 revised Dairy scene.
 *  This revision updates the look we created for a December, 2014
 *  weavers' conference at Boulder's Dairy Center for the Arts (thedairy.org).
 *
 *  An Udder scenegraph has as its root a Mixer object, and each layer in the
 *  scene is backed by a Layer child of that Mixer.
 */
public abstract class DairyScene {

    /** Instantiate a new, site-specific scene in the form of a Mixer. */
    public static Mixer create(Device[] devices) {
        BlendOp max = new MaxBlendOp();

        // Add layers from bottom (background) to top (foreground),
        // in order of composition.
        ArrayList<Mixable> layers = new ArrayList<Mixable>();

        // The background is additive (unlike the gel layer
        // below), so add color globally using this level.
        Layer background = new Layer("Background",
        new MonochromeEffect(Pixel.black()));
        background.setBlendOp(max);
        layers.add(background);

        // The woven effect periodically reappears when the
        // mixer's shuffler is running. It is our signature look.
        Layer woven = new Layer("Woven", new WovenEffect());
        woven.setBlendOp(max);
        layers.add(woven);
        int wovenLayerIndex = 1;

        // The next several layers are all sequenced textures and blooms.
        // They are sorted so that any two or three adjacent
        // layers look good together. When the mixer's shuffler
        // is running, pairs and trios of adjacent layers
        // appear together, fading in and out.
        String adir = "images" + separator + "dairy_collection_A_720p" + separator;
        String loopdir = "images" + separator + "dairy_collection_B_scrolling_loops" + separator;

        // Warning: adding a few more layers than this may overload some
        // (currently unknown) stage of the pipeline when run on the
        // Dairy's little Beaglebone server. This causes quasiperiodic
        // sticky-looking frame drops of usually about 1/3 second every
        // approx. 4 sec. These are especially visible when the shuffled
        // texture layers are visible, but you can also see in the Woven
        // effect. The Fadecandy's interpolation tries to paper over the
        // missing frames, but it hurts the brain to look at. I speculate
        // that this has something to do with thread scheduling, because
        // I've never been able to reproduce this bug on my multicore
        // MBP. Especially weird is that dropping the frame rate by 50%
        // or even 75% doesn't seem to change the sticky playback. That is
        // big clue #1. Two more  clues: 2) the CPU is not quite maxxed out
        // (according to htop) while this bug is visible, and running htop
        // or not (which occupies 9% CPU itself) doesn't obviously change
        // the stickiness. 3) The logs never report dropped frames, leading
        // me to speculate that Udder never rendered those frames to begin
        // with.

        RollEffect r;
        ArrayList<EffectSlot> fx = new ArrayList<EffectSlot>();

        r = new RollEffect(loopdir + "flame_scroller_amber_medium_contrast.png");
        r.setYPeriodMillis(1450);
        fx.add(new EffectSlot(r, 1.0, 0.45));

        // blue lightning, good reviews from BV, blends well with flame
        fx.add(new EffectSlot(new TextureEffect(adir + "blue_skull_necklace.png"), 1.0, 1.0));

        // should blend nicely with blue above
        fx.add(new EffectSlot(new TextureEffect(adir + "light_cyan_trigrams.png"), 0.2, 1.0));

        // mix all three similar looks, this + 2 above. verified good in person.
        fx.add(new EffectSlot(new TextureEffect(adir + "light_amber_trigrams.png"), 1.0, 1.0));

        // (mix early, keep late) <<< tree shadows, try to solo on outro
        fx.add(new EffectSlot(new TextureEffect(adir + "coppertone_trigrams.png"), 1.0, 1.0));

        // flames descend suddenly on coppertone tree shadows
        r = new RollEffect(loopdir + "flame_scroller_amber_medium_contrast.png");
        r.setYPeriodMillis(3700);
        fx.add(new EffectSlot(r, 0.35, 0.7));

        // nice palette, nice interaction, might need to be even brighter
        fx.add(new EffectSlot(new TextureEffect(adir + "medium_contrast" + separator + "redblue_triclops_medium_contrast.png"), 1.0, 1.0));

        // purple sparks, brings out copper, full enough but still dim
        fx.add(new EffectSlot(new TextureEffect(adir + "rose_tint_trigrams.png"), 1.0, 1.0));

        r = new RollEffect(loopdir + "cartoon_flame_scroller_horizontal_gaussian_blur_4.2.png");
        r.setXPeriodMillis(4300);
        fx.add(new EffectSlot(r, 0.5, 0.4));

        r = new RollEffect(loopdir + "rainbow_stupidity_scroller_wavy.png");
        r.setYPeriodMillis(5130);
        fx.add(new EffectSlot(r, 0.35, 0.45));

        fx.add(new EffectSlot(new TextureEffect(adir + "light_amber_densely_mustachioed_cthulus.png"), 0.5, 0.5));

        // like water (could also flip x or y axis)
        r = new RollEffect(loopdir + "cartoon_rivulet_scroller_horizontal_gaussian_blur_4.2.png");
        r.setXPeriodMillis(6600);
        fx.add(new EffectSlot(r, 0.5, 0.5));

        BloomEffect b = new BloomEffect();
        Pixel[] cool2WayPalette = {
            new Pixel(0.11f, 0.05f, 1.0f), // purple
            new Pixel(0.0f, 0.85f, 0.1f) // green
        };
        b.setState(new BloomEffectState(cool2WayPalette, true, true, true, true));
        fx.add(new EffectSlot(b, 1.0, 1.0));

        fx.add(new EffectSlot(new TextureEffect(adir + "purple_chains.png"), 1.0, 1.0));
        fx.add(new EffectSlot(new TextureEffect(adir + "yellow_tape_worms.png"), 1.0, 0.9));

        // clown stripes: 3 colors, full reflective symmetry, both axes
        if(false) {
            // skipping this because it was too much of a trainwreck, and
            // AV and BV reported that yello tape worms + transitional2WayPalette
            // bloom were very nice
            b = new BloomEffect();
            Pixel[] mixed3WayPalette = {
                    new Pixel(1.0f, 0.8f, 0.0f), // yellow
                    new Pixel(0.85f, 0.6f, 0.00f), // orange
                    new Pixel(0.0f, 0.0f, 0.75f), // medium blue
            };
            b.setState(new BloomEffectState(mixed3WayPalette, true, true, true, true));
            fx.add(new EffectSlot(b, 1.0, 0.8));
        }

        // two colors, simpler symmetry, both axes
        b = new BloomEffect();
        Pixel [] transitional2WayPalette = {
            new Pixel(0.0f, 0.80f, 1.0f), // cyan
            new Pixel(0.0f, 0.0f, 1.0f) // blue
        };
        b.setState(new BloomEffectState(transitional2WayPalette, true, true, false, true));
        fx.add(new EffectSlot(b, 0.6, 0.7));

        // two colors, simplified to just about an eighth of a blooming leaf, one axis
        b = new BloomEffect();
        Pixel [] dualPalette = {
            new Pixel(1.0f, 0.66f, 0.10f), // amber/orange
            new Pixel(0.0f, 0.0f, 0.45f), // dim blue
        };

        // (this looked even better with pure black and white, but too intense for this setting)
        b.setState(new BloomEffectState(dualPalette, false, false, true, false));
        fx.add(new EffectSlot(b, 0.5, 1.0));

        int sequenceStartIndex = layers.size();

        // Communicate per-layer fade-in and fade-out timings to the shuffler
        int expectedLayerCount = layers.size() + fx.size() + 1;
        DairyShufflerFadeTiming[] timings = new DairyShufflerFadeTiming[expectedLayerCount];

        for(int i=0; i<fx.size(); i++) {
            EffectSlot slot = fx.get(i);
            String layerName = slot.effect.getClass().getSimpleName();
            if(slot.effect instanceof ImageEffect) {
                layerName += " " + ((ImageEffect)slot.effect).getFilename();
            }
            Layer layer = new Layer(layerName, slot.effect);
            layer.setBlendOp(max);
            layers.add(layer);
            timings[i + sequenceStartIndex] = slot.timing;
        }
        int sequenceEndIndex = layers.size() - 1;

        // Example: chase effect (removed from the production version
        // of DairyScene because it was not part of the show).
        // ----------------------------------------------------------
        // A chase that runs over the devices in patch sheet order
        // (not spatial order, not OPC address order).
        // Requires an external raster to display anything.
        // A useful test pattern generator because in its default config,
        // it scrolls the chase by a single pixel at a time.
        // Layer chase = new Layer("Chase", new ChaseEffect(null));
        // chase.setBlendOp(max);
        // layers.add(chase);

        // A user may supply an array of colors to map directly onto the
        // rig with these external inputs.
        Layer externalA = new Layer("External input A", new ArrayEffect(null));
        externalA.setBlendOp(max);
        layers.add(externalA);

        // Example: add a second external input (disabled for now, to
        // maximize performance).
        // -------------------------------------------------------------
        // Layer externalB = new Layer("External input B", new ArrayEffect(null));
        // externalB.setBlendOp(max);
        // layers.add(externalB);

        // Gel example (removed from the production version of the DairyScene
        // because we didn't wind up using it in the show).
        // To simulate a gel, add a mult layer atop other content.
        // ------------------------------------------------------------------
        // In the mult blendop, white=transparent. Tint
        // everything globally by adjusting this color.
        // Layer gel = new Layer("Color correction gel",
        //     new MonochromeEffect(Pixel.white()));
        // gel.setBlendOp(new MultiplyBlendOp());
        // layers.add(gel);

        Mixer mixer = new Mixer((Collection<Mixable>) layers);
        mixer.patchDevices(devices);
        System.out.println("Patched " + devices.length
            + " devices to the DairyScene's Mixer.");

        for(Mixable layer: mixer) {
            layer.setLevel(0.0);
        }
        mixer.setLevel(1.0);

        System.out.println(mixer.getDescription());
        System.out.println("Shuffled sequence start layer: "
                + sequenceStartIndex + " end layer: " + sequenceEndIndex);

        // This shuffler will subscribe itself to the mixer, which therefore
        // retains a reference. The shuffler retains a backref to the mixer.
        new DairyShuffler(mixer, wovenLayerIndex, sequenceStartIndex, sequenceEndIndex, timings);

        return mixer;
    }

}


class EffectSlot {
    public Effect effect = null;
    public DairyShufflerFadeTiming timing = null;

    public EffectSlot(Effect effect, double in, double out) {
        this.effect = effect;
        this.timing = new DairyShufflerFadeTiming(in, out);
    }
}
