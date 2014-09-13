package com.coillighting.udder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.coillighting.udder.Mixer;
import com.coillighting.udder.MonochromeEffect;
import com.coillighting.udder.PatchElement;


/** Define the scenegraph for the December, 2014 weavers' conference at
 *  Boulder's Dairy Center for the Arts (thedairy.org). An Udder scenegraph
 *  has as its root a Mixer object, and each layer in the scene is backed by
 *  a Layer child of that Mixer.
 */
public abstract class DairyScene {

	/** Instantiate a new scene in the form of a Mixer. */
	public static Mixer create(List<PatchElement> patchElements) {
		// A basic three-layer look to get started.
		Layer background = new Layer("Background", new MonochromeEffect());
		Layer rainbowStupidity = new Layer("Rainbow stupidity",
			new MonochromeEffect()); // TODO: gradient effect here
		Layer gel = new Layer("Gel", new MonochromeEffect()); // TODO: multiply blend mode

		// Add layers from bottom (background) to top (foreground), i.e. in
		// order of composition.
		ArrayList<Mixable> layers = new ArrayList<Mixable>(3);
		layers.add(background);
		layers.add(rainbowStupidity);
		layers.add(gel);

		// TODO relocate this, sort out address mapping
		List<Device> devices = new ArrayList(patchElements.size());
		int addr = 0;
		for(PatchElement pe : patchElements) {
			Device device = pe.toDevice(addr);
			System.err.println(device); //TEMP
			devices.add(device);
			++addr;
		}
		for(Mixable layer : layers) {
			layer.patchDevices(devices);
		}
		System.err.println("Patched " + addr + " devices to the DairyScene's Mixer.");

		return new Mixer((Collection<Mixable>) layers);
	}

}
