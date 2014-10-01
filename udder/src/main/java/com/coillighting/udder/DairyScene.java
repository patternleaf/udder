package com.coillighting.udder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Define the scenegraph for the December, 2014 weavers' conference at
 *  Boulder's Dairy Center for the Arts (thedairy.org). An Udder scenegraph
 *  has as its root a Mixer object, and each layer in the scene is backed by
 *  a Layer child of that Mixer.
 */
public abstract class DairyScene {

	/** Instantiate a new scene in the form of a Mixer. */
	public static Mixer create(List<Device> devices) {
		BlendOp max = new MaxBlendOp();

		// A basic three-layer look to get started.
		Layer background = new Layer("Background",
			new MonochromeEffect(new Pixel(1.0f, 0.5f, 0.0f)));
		background.setBlendOp(max);

		Layer rainbowStupidity = new Layer("Rainbow stupidity",
			new MonochromeEffect(new Pixel(0.0f, 0.0f, 0.0f))); // TODO: gradient effect here
		rainbowStupidity.setBlendOp(max);

		Layer externalRaster = new Layer("External input",
			new RasterEffect(null));
		externalRaster.setBlendOp(max);

		Layer gel = new Layer("Gel",
			new MonochromeEffect(new Pixel(0.0f, 0.0f, 0.0f))); // TODO: multiply blend mode
		gel.setBlendOp(max);

		// Add layers from bottom (background) to top (foreground), i.e. in
		// order of composition.
		ArrayList<Mixable> layers = new ArrayList<Mixable>(3);
		layers.add(background);
		layers.add(rainbowStupidity);
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
