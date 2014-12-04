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
        String [] sequencedTextures = {
                "blue_skull_necklace.png",
                "green_gilled_lace.png",
                "yellow_antennae.png",
                "cyan_chains.png",
                "magenta_loops.png",
                "yellow_lavender_calligraphic.png",
                "purple_chains.png",
                "amber_mustachioed_cthulus.png",
                "orange_mustachioed_cthulus.png",
                "purple_blue_chains.png",
                "red_triclops_minimal.png",
                "red_triclops_embellished.png",
                "light_blue_calligraphemes.png",
                "lavender_propellers.png",
                "skyblue_loops.png",
                "mauve_taupe_worms.png",
                "redblue_triclops.png",
        };

        int sequenceStartIndex = layers.size();
        for(String filename: sequencedTextures) {
            Layer texture = new Layer("Texture " + filename,
                new TextureEffect(
                    "images" + separator + "dairy_collection_A_720p" + separator + filename));
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
