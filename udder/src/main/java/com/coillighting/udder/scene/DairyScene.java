package com.coillighting.udder.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.blend.MultiplyBlendOp;
import com.coillighting.udder.effect.MonochromeEffect;
import com.coillighting.udder.effect.RasterEffect;
import com.coillighting.udder.effect.woven.WovenEffect;
import com.coillighting.udder.Device;
import com.coillighting.udder.mix.*;
import com.coillighting.udder.Pixel;

/** Define the scenegraph for the December, 2014 weavers' conference at
 *  Boulder's Dairy Center for the Arts (thedairy.org). An Udder scenegraph
 *  has as its root a Mixer object, and each layer in the scene is backed by
 *  a Layer child of that Mixer.
 */
public abstract class DairyScene {

	/** Instantiate a new scene in the form of a Mixer. */
	public static Mixer create(List<Device> devices) {
		BlendOp max = new MaxBlendOp();
		BlendOp mult = new MultiplyBlendOp();

		// A basic three-layer look to get started.
		Layer background = new Layer("Background",
			new MonochromeEffect(Pixel.black()));
		background.setBlendOp(max);

		Layer woven = new Layer("Woven", new WovenEffect());
		woven.setBlendOp(max);

		Layer externalRaster = new Layer("External input",
			new RasterEffect(null));
		externalRaster.setBlendOp(max);

		Layer gel = new Layer("Gel", new MonochromeEffect(Pixel.black()));
		gel.setBlendOp(mult);

		// Add layers from bottom (background) to top (foreground), i.e. in
		// order of composition.
		ArrayList<Mixable> layers = new ArrayList<Mixable>(3);
		layers.add(background);
		layers.add(woven);
		layers.add(externalRaster);
		layers.add(gel);

		Mixer mixer = new Mixer((Collection<Mixable>) layers);
		mixer.patchDevices(devices);
		System.err.println("Patched " + devices.size()
			+ " devices to the DairyScene's Mixer.");

		for(Mixable layer: mixer) {
			layer.setLevel(0.0f);
		}
		externalRaster.setLevel(1.0f);
		mixer.setLevel(1.0f);

		return mixer;
	}

}
