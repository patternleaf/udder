package com.coillighting.udder.effect.woven;

import com.coillighting.udder.Pixel;

/** A simple, public data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int warp_width = 7;
    protected int weft_height = 7;

    /** A single pixel maps onto the whole background. */
    public Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
    public Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
    public Pixel[][] weft = null; // [x][y]

    public WovenFrame() {
        this.reset();
    }

    /** Without reallocating pixels, set their colors a uniform value. */
    public void setColor(Pixel color) {
        background.setColor(color);
        for(int x=0; x<warp.length; x++) {
            warp[x].setColor(color);
        }
        for(int x=0; x<weft.length; x++) {
            for(int y=0; y<weft[x].length; y++) {
                weft[x][y].setColor(color);
            }
        }
    }

    /** Reallocate all pixels and set them to black. If you change warp_width
     *  or weft_height, this is where it will take effect.
     */
    public void reset() {
        background = Pixel.black();

        warp = new Pixel[warp_width];
        for(int x=0; x<warp.length; x++) {
            warp[x] = Pixel.black();
        }

        weft = new Pixel[2][weft_height];
        for(int x=0; x<weft.length; x++) {
            for(int y=0; y<weft[x].length; y++) {
                weft[x][y] = Pixel.black();
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("background " + background.toHexRGB()
            + "\nwarp       ");

        for(int x=0; x<warp.length; x++) {
            sb.append(warp[x].toHexRGB()).append(' ');
        }
        sb.append("\nweft       ");

        for(int y=0; y<weft[0].length; y++) {
            if(y > 0) {
                sb.append("\n           ");
            }
            for(int x=0; x<weft.length; x++) {
                sb.append(weft[x][y].toHexRGB()).append(' ');
            }
        }
        sb.append('\n');
        return sb.toString();
     }

}
