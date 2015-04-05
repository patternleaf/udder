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

    /** Instantiate a new scene in the form of a Mixer. */
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

        RollEffect r;
        ArrayList<EffectSlot> fx = new ArrayList<EffectSlot>();

        r = new RollEffect(loopdir + "flame_scroller_high_key_high_contrast.png");
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
        r = new RollEffect(loopdir + "flame_scroller_high_key_low_contrast.png");
        r.setYPeriodMillis(3700);
        fx.add(new EffectSlot(r, 0.35, 0.7));

        // nice palette, nice interaction, might need to be even brighter
        fx.add(new EffectSlot(new TextureEffect(adir + "medium_contrast" + separator + "redblue_triclops_medium_contrast.png"), 1.0, 1.0));

        // purple sparks, brings out copper, full enough but still dim
        fx.add(new EffectSlot(new TextureEffect(adir + "rose_tint_trigrams.png"), 1.0, 1.0));

        // TODO also try blurry versions (maybe flip h or v?)
        r = new RollEffect(loopdir + "cartoon_flame_scroller_horizontal_gaussian_blur_4.2.png");
        r.setXPeriodMillis(4300);
        fx.add(new EffectSlot(r, 0.5, 0.4));

        // (mix latish) - maybe tone down primary reds in these a little more? check.
        r = new RollEffect(loopdir + "rainbow_stupidity_scroller_wavy.png");
        r.setYPeriodMillis(5100);
        fx.add(new EffectSlot(r, 0.35, 0.45));

        // ?? amber_mustachioed_cthulus.png (desparsify, hue-diversify background? TODO)
        fx.add(new EffectSlot(new TextureEffect(adir + "amber_mustachioed_cthulus.png"), 0.5, 0.5));

        // like water (perhaps try flipping h or v?)
        r = new RollEffect(loopdir + "cartoon_rivulet_scroller_horizontal_gaussian_blur_4.2.png");
        r.setXPeriodMillis(6600);
        fx.add(new EffectSlot(r, 0.5, 0.5));

        // TODO set palette, something coming out of water
        // for bloom: green and orange? (maybe elsewhere, old note) TODO
        BloomEffect b = new BloomEffect();
        Pixel[] cool2WayPalette = {
            new Pixel(0.11f, 0.05f, 1.0f), // purple
            new Pixel(0.0f, 0.85f, 0.1f) // green
        };
        b.setState(new BloomEffectState(cool2WayPalette, true, true, true, true));
        fx.add(new EffectSlot(b, 1.0, 1.0));

        // purple_chains.png - but fill in black holes "subtle but tasteful w/ deep violet" kind of dark
        // see also purple_blue_chains, which got cut previously.
        // (maybe use warmed skyblue_loops as subtle amber overlay?)
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
        // (this looked even better with pure black and white, but intense for this setting)
        b.setState(new BloomEffectState(dualPalette, false, false, true, false));
        fx.add(new EffectSlot(b, 0.5, 1.0));

        int sequenceStartIndex = layers.size();

        // Communicate per-layer fade-in and fade-out timings to the shuffler
        int expectedLayerCount = layers.size() + fx.size() + 1; // kludge, sorry
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

// Old scenes, ideas, and notes

// This playlist looked nice, and it played back smoothly, but there
// weren't enough open patterns between the dark, detailed ones to
// sufficiently illuminate the whole structure.
        /*
        String [] sequencedTextures_A = {
                adir + "blue_skull_necklace.png", // ** blue lightning, good reviews from BV
                adir + "green_gilled_lace.png",
                adir + "yellow_antennae.png",
                adir + "cyan_chains.png",
                adir + "magenta_loops.png",
                adir + "yellow_lavender_calligraphic.png",
                adir + "purple_chains.png",
                adir + "amber_mustachioed_cthulus.png", // too sparse, might modify
                adir + "orange_mustachioed_cthulus.png",  // too sparse, might modify
                adir + "purple_blue_chains.png", // see also its twin
                adir + "red_triclops_minimal.png", // too minimal
                adir + "red_triclops_embellished.png",
                adir + "light_blue_calligraphemes.png", // probably twinkly, nonreflective off copper
                adir + "lavender_propellers.png", // overlay
                adir + "skyblue_loops.png", // for an overlay?
                adir + "mauve_taupe_worms.png", // scheduled
                adir + "redblue_triclops.png", // scheduled, boost brightness
        };
        */

        /*
ideas
-----
flame_scroller_high_key_high_contrast > (orange, mix early, keep late) because people are scared of the dark
blue_skull_necklace  > (mix late) mostly alone
light_cyan_trigrams  | (mix late) [should blend nicely with blue above]
light_amber_trigrams | (mix early) mix all three. verified good.
coppertone_trigrams  | (mix early, keep late)                 <<< tree shadows, try to solo on outro

flame_scroller_high_key_low_contrast > bright amber, return of the flame

redblue_triclops - boost blue brightness? or use med contrast version? nice palette, nice interaction but too dim

rose_tint_trigrams purple sparks, brings out copper, full enough but still dim

cartoon_flame_scroller_horizontal_* (mix earlyish, maybe flip v?)

rainbow stupidity 1 (mix latish) - maybe tone down primary reds in these a little more? check.

?? amber_mustachioed_cthulus.png (desparsify, hue-diversify background?)

cartoon_rivulet_scroller_horizontal_* (maybe flip h or v?)

rainbow stupidity 2 (mix latish)

purple_chains.png - but fill in black holes "subtle but tasteful w/ deep violet" kind of dark
    see also purple_blue_chains, which got cut previously
    (maybe use warmed skyblue_loops as subtle amber overlay?)
mauve_taupe_worms - not bad on metal, pinkish

pastel rainbow stupidity here?
- or -
flame_scroller_low_key_high_contrast > more amber, return of the flame
- or -
something not made yet? colorful, structured to fit with the weave -- stacked hexes + blur? simple v-scroll?
        */


// The following playlist sufficiently illuminates the structure,
// but the nice, detailed motion gets washed out because there
// are so many open patterns.
//
// Warning: adding a few more layers than this overloads some
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
        /*
        String [] sequencedTextures_AandB = {
                bdir + "flames.jpg", // 2
                adir + "amber_mustachioed_cthulus.png", // 3 nice effect but rather sparse
                bdir + "flower_pedals.jpg", // 4 offensive
                adir + "rose_tint_trigrams.png", // 5 ** purple sparks, looks good, brings out copper, full enough, dim but distinct look
                bdir + "luminous_purple_flower.jpg", // 6
                // **? 5 + 7: purple twinkles + amber/purple twinkles? ok, lots of motion, full, twinkly, not actually in the playlist though
                adir + "coppertone_trigrams.png", // 7 ** nice copper mode. tree shadows in wind.
                bdir + "orange_clouds.jpg", // 8
                adir + "light_amber_trigrams.png", // 9 ** light amber shimmers, sort of a flattering brown on the metal strips, nice contrast between various strips.
                bdir + "pink_yellow_orange_flowers.jpg", // 10 clown barf, wtf?
                // ** 9 + 11 + 7 looks good
                adir + "light_cyan_trigrams.png", // 11 light blue "lightning" -- maybe not as electric as the pure blue one they cut. nice when it jumps fast.
                bdir + "orange_green_geometry.jpg", // 12
                adir + "orange_mustachioed_cthulus.png", // 13 too many black gaps, maybe too primary? notes are conflicted.
                bdir + "purple_on_green_flower.jpg", // 14
                adir + "redblue_triclops.png", // 15 red spots with violet undertones, interacts nicely with the rig but rather sparse/dim. palette good.. hints of magenta in there.
                bdir + "space_clouds.jpg", // 16
                adir + "green_gilled_lace.png", // 17 too green, too flickery, twinkles
                bdir + "trippy.jpg", // 18 tame the primary red blotch!
                adir + "red_triclops_minimal.png", // 19 primary red doesn't cut it
                bdir + "warm_cool_weave.jpg", // 20
                adir + "purple_chains.png", // 21 ** deep violet, maybe black holes too big in places esp Sw, subtle but tasteful twinkle effect.
                bdir + "warped_squares.jpg", // 22
                adir + "light_amber_trigrams.png", // ** 23 looks good, monochomatic shadowplay on rig, brings out metals like previous similar one. some black gaps too big sometimes
                bdir + "water_orange.jpg", // 24
                adir + "mauve_taupe_worms.png", // ** 25 not bad on metal, makes some look red and some purple, rest pink. all pink would be bad.
                bdir + "white_yellow_flowers.jpg", // 26
                adir + "yellow_antennae.png", // 27 looks okay on metal, not very bright, this yellow BTW reads just slightly warm esp on metal
                bdir + "yellow_on_green_flowers.jpg", // 28
        };
        */


class EffectSlot {
    public Effect effect = null;
    public DairyShufflerFadeTiming timing = null;

    public EffectSlot(Effect effect, double in, double out) {
        this.effect = effect;
        this.timing = new DairyShufflerFadeTiming(in, out);
    }
}
