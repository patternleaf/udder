package com.coillighting.udder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.coillighting.udder.Device;
import com.coillighting.udder.Mixer;
import com.coillighting.udder.MonochromeEffect;


/** Define the scenegraph for the December, 2014 weavers' conference at
 *  Boulder's Dairy Center for the Arts (thedairy.org). An Udder scenegraph
 *  has as its root a Mixer object, and each layer in the scene is backed by
 *  a Layer child of that Mixer.
 */
public abstract class DairyScene {

	/** Instantiate a new scene in the form of a Mixer. */
	public static Mixer create(List<Device> devices) {
		// A basic three-layer look to get started.
		Layer background = new Layer("Background",
			new MonochromeEffect(new Pixel(0.0, 0.0, 0.0)));

		Layer rainbowStupidity = new Layer("Rainbow stupidity",
			new MonochromeEffect(new Pixel(0.0, 0.0, 0.0))); // TODO: gradient effect here

		Layer gel = new Layer("Gel",
			new MonochromeEffect(new Pixel(0.0, 0.0, 0.0))); // TODO: multiply blend mode

		// Add layers from bottom (background) to top (foreground), i.e. in
		// order of composition.
		ArrayList<Mixable> layers = new ArrayList<Mixable>(3);
		layers.add(background);
		layers.add(rainbowStupidity);
		layers.add(gel);

		for(Mixable layer : layers) {
			layer.patchDevices(devices);
		}
		System.err.println("Patched " + devices.size()
			+ " devices to the DairyScene's Mixer.");

		return new Mixer((Collection<Mixable>) layers);
	}

}
