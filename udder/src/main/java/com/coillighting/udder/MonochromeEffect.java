package com.coillighting.udder;

import java.util.List;

import com.coillighting.udder.Effect;


public class MonochromeEffect implements Effect {

	private boolean dirty = false;
	private Pixel color = null;
	private Pixel[] pixels = null;
	private Device[] devices = null;

	public MonochromeEffect(Pixel color) {
		this.setColor(color);
	}

	public void setColor(Pixel color) {
		// Do not dirty this effect if color hasn't actually changed.
		if(color == null) {
			throw new NullPointerException("MonochromeEffect requires a color.");
		} else if(this.color == null || !this.color.equals(color)) {
			this.color = new Pixel(color);
			this.dirty = true;
		}
	}

	public void animate(TimePoint timePoint) {
		if(this.dirty) {
			if(this.pixels != null) {
				for(int i=0; i<this.pixels.length; i++) {
					this.pixels[i].setColor(this.color);
				}
			}
			this.dirty = false;
		}
	}

	public Pixel[] render() {
		// TODO spell out borrowing contract for render! borrow must not modify.
		// Probably do this with an (Immutable) Pixel & MutablePixel.
		return this.pixels;
	}

	/** Reinitialize the raster to match the size of the new patch sheet. */
	public void patchDevices(List<Device> devices) {
		int length = devices.size();
		if(length > 0) {
			this.devices = new Device[length];
			for(int i=0; i<this.devices.length; i++) {
				this.devices[i] = devices.get(i);
			}
		} else {
			this.devices = null;
		}
		this.initPixels(length);
	}

	protected void initPixels(int length) {
		if(length > 0) {
			this.pixels = new Pixel[length];
			for(int i=0; i<this.pixels.length; i++) {
				this.pixels[i] = new Pixel();
			}
		} else {
			this.pixels = null;
		}
	}
}
