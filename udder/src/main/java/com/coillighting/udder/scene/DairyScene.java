package com.coillighting.udder.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.io.File.separator;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.blend.MultiplyBlendOp;
import com.coillighting.udder.effect.MonochromeEffect;
import com.coillighting.udder.effect.ChaseEffect;
import com.coillighting.udder.effect.ArrayEffect;
import com.coillighting.udder.effect.TextureEffect;
import com.coillighting.udder.effect.woven.WovenEffect;
import com.coillighting.udder.mix.Layer;
import com.coillighting.udder.mix.Mixable;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** Define the scenegraph for the December, 2014 weavers' conference at
 *  Boulder's Dairy Center for the Arts (thedairy.org). An Udder scenegraph
 *  has as its root a Mixer object, and each layer in the scene is backed by
 *  a Layer child of that Mixer.
 */
public abstract class DairyScene {

    /** Instantiate a new scene in the form of a Mixer. */
    public static Mixer create(Device[] devices) {
        BlendOp max = new MaxBlendOp();
        BlendOp mult = new MultiplyBlendOp();

        // Add layers from bottom (background) to top (foreground),
        // in order of composition.
        ArrayList<Mixable> layers = new ArrayList<Mixable>();

        // A basic three-layer look to get started.

        // The background is additive (unlike the gel layer
        // below), so add color globally using this level.
        Layer background = new Layer("Background",
            new MonochromeEffect(Pixel.black()));
        background.setBlendOp(max);
        layers.add(background);

        // The woven effect periodically reappears when the
        // mixer's shuffler is running.
        Layer woven = new Layer("Woven", new WovenEffect());
        woven.setBlendOp(max);
        layers.add(woven);
        int wovenLayerIndex = 1;

        // Currently layers 3-19 are all sequenced textures.
        // They are sorted so that any two to four adjacent
        // layers look good together. When the mixer's shuffler
        // is running, pairs and trios of adjacent layers
        // appear together, fading in and out.
        String adir = "dairy_collection_A_720p" + separator;
        String bdir = "dairy_collection_B" + separator;

        // This playlist looks nice, and it plays back smoothly, but there
        // aren't enough open patterns between the dark, detailed ones to
        // sufficiently illuminate the whole structure.
        String [] sequencedTextures_A = {
                adir + "blue_skull_necklace.png",
                adir + "green_gilled_lace.png",
                adir + "yellow_antennae.png",
                adir + "cyan_chains.png",
                adir + "magenta_loops.png",
                adir + "yellow_lavender_calligraphic.png",
                adir + "purple_chains.png",
                adir + "amber_mustachioed_cthulus.png",
                adir + "orange_mustachioed_cthulus.png",
                adir + "purple_blue_chains.png",
                adir + "red_triclops_minimal.png",
                adir + "red_triclops_embellished.png",
                adir + "light_blue_calligraphemes.png",
                adir + "lavender_propellers.png",
                adir + "skyblue_loops.png",
                adir + "mauve_taupe_worms.png",
                adir + "redblue_triclops.png",
        };

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
        String [] sequencedTextures_AandB = {
                bdir + "flames.jpg",
                adir + "amber_mustachioed_cthulus.png",
                bdir + "flower_pedals.jpg",
                adir + "rose_tint_trigrams.png",
                bdir + "luminous_purple_flower.jpg",
                adir + "coppertone_trigrams.png",
                bdir + "orange_clouds.jpg",
                adir + "light_amber_trigrams.png",
                bdir + "pink_yellow_orange_flowers.jpg",
                adir + "light_cyan_trigrams.png",
                bdir + "orange_green_geometry.jpg",
                adir + "orange_mustachioed_cthulus.png",
                bdir + "purple_on_green_flower.jpg",
                adir + "redblue_triclops.png",
                bdir + "space_clouds.jpg",
                adir + "green_gilled_lace.png",
                bdir + "trippy.jpg",
                adir + "red_triclops_minimal.png",
                bdir + "warm_cool_weave.jpg",
                adir + "purple_chains.png",
                bdir + "warped_squares.jpg",
                adir + "light_amber_trigrams.png",
                bdir + "water_orange.jpg",
                adir + "mauve_taupe_worms.png",
                bdir + "white_yellow_flowers.jpg",
                adir + "yellow_antennae.png",
                bdir + "yellow_on_green_flowers.jpg",
        };

        // TEMP for the dairy
        String [] sequencedTextures = sequencedTextures_AandB;

        int sequenceStartIndex = layers.size();
        for(String filename: sequencedTextures) {
            Layer texture = new Layer("Texture " + filename,
                new TextureEffect(
                    "images" + separator + filename));
            texture.setBlendOp(max);
            layers.add(texture);
        }
        int sequenceEndIndex = layers.size() - 1;

        // A chase that runs over the devices in patch sheet order
        // (not spatial order, not OPC address order).
        // Requires an external raster to display anything.
        // A useful test pattern generator because in its default config,
        // it scrolls the chase by a single pixel at a time.
        Layer chase = new Layer("Chase", new ChaseEffect(null));
        chase.setBlendOp(max);
        layers.add(chase);

        // Supply an array of colors to be directly
        // mapped onto the rig with these two layers.
        Layer externalA = new Layer("External input A", new ArrayEffect(null));
        externalA.setBlendOp(max);
        layers.add(externalA);

        Layer externalB = new Layer("External input B", new ArrayEffect(null));
        externalB.setBlendOp(max);
        layers.add(externalB);

        // In the mult blendop, white=transparent. Tint
        // everything globally by adjusting this color.
        Layer gel = new Layer("Color correction gel", new MonochromeEffect(Pixel.white()));
        gel.setBlendOp(mult);
        layers.add(gel);

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
        new DairyShuffler(mixer, wovenLayerIndex, sequenceStartIndex, sequenceEndIndex);

        return mixer;
    }

}
