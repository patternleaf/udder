package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;

/** A simple, public data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int width = 7;
    protected int height = 7;

    /** A single pixel maps onto the whole background. */
	public Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
	public Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
	public Pixel[][] weft = null; // [x][y]

	public WovenFrame() {
		this.reset();
	}

	public void reset() {
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

    public String toString() {
    	StringBuffer sb = new StringBuffer("background " + background.toRGB()
    		+ " = " + background.toRGBA() + "\n"); //TEMP-TEST

    	sb.append("warp       ");
        for(int x=0; x<warp.length; x++) {
            sb.append(warp[x].toRGB()).append(' ');
        }
        sb.append("\n");

        sb.append("weft       ");
        for(int y=0; y<weft[0].length; y++) {
            if(y > 0) {
                sb.append("\n           ");
            }
            for(int x=0; x<weft.length; x++) {
                sb.append(weft[x][y].toRGB()).append(' ');
            }
        }
        sb.append("\n");
        return sb.toString();
     }

}
