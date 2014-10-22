package com.coillighting.udder.effect.woven;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;

/** A simple, public data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int warpThreadcount = 7; // width is approx. double this
    protected int weftThreadcount = 32;

    /** A single pixel maps onto the whole background. */
    public Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
    public Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
    public Pixel[][] weft = null; // [x][y]

    public WovenFrame() {
        this.reset();
    }

    public void scaleColor(double scale) {
        // TODO: decide whether we can move to 64 bpc pixels without hurting 32 bit raspis
        float f = (float) scale;
        background.scale(f);
        for(Pixel p: warp) {
            p.scale(f);
        }
        for(int x=0; x<2; x++) {
            for(Pixel p: weft[x]) {
                p.scale(f);
            }
        }
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

    /** Reallocate all pixels and set them to black. If you change warpThreadcount
     *  or weftThreadcount, this is where it will take effect.
     */
    public void reset() {
        background = Pixel.black();

        // There is a blank (warp background color) color between each pair
        // of warp threads, so multiply by 2. Make sure the edges are filled,
        // so subtract 1 at the end.
        warp = new Pixel[(2 * warpThreadcount) - 1];
        for(int x=0; x<warp.length; x++) {
            warp[x] = Pixel.black();
        }

        // Ditto for the weft threads.
        weft = new Pixel[2][weftThreadcount];
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

    public void render(Pixel[] pixels, Device[] devices) {
        BoundingBox box = Device.getDeviceBoundingBox(devices);
        double wScale = 1.0 / box.getWidth();
        double hScale = 1.0 / box.getHeight();
        double xOff = -box.getMinX();
        double yOff = -box.getMinY();

        for(int i=0; i<devices.length; i++) {
            // Scale this pixel's device coordinates into the unit square.
            Point3D pt = devices[i].getPoint3D();

            double px = wScale * (pt.getX() + xOff); // normalized 0..1
            double py = hScale * (pt.getY() + yOff); // ditto
            // TODO account for z or group?

            // System.err.println("px " + px + " = wScale " + wScale + " * (pt.getX() " + pt.getX() + " + xOff " + xOff + ") warpLen=" + warp.length);

            // Draw the nearest neighbor in the warp for this pixel.
            double warpScale = -0.000000001 + (double) warp.length;
            int xWarp = (int)(px * warpScale);
            pixels[i].setColor(warp[xWarp]);

            // System.err.println("" + px + " * " + warpScale + " = " + (px * warpScale) + " => " + xWarp);

            // System.err.println("set pixels[" + i + "] (warp " + xWarp
            //     + " warpScale=" + warpScale + " warpLen=" + warp.length
            //     + " px=" + px + " py=" + py
            //     + " pt.getX=" + pt.getX() + " pt.getY=" + pt.getY()
            //     + " boxw=" + box.getWidth() + " wScale=" + wScale
            //     + " boxh=" + box.getHeight() + " hScale=" + hScale
            //     + " minX=" + box.getMinX() + " xOff=" + xOff
            //     + " minY=" + box.getMinY() + " yOff=" + yOff + ") = "
            //     + pixels[i]);

            // Draw the nearest neighbor in the weft for this pixel.
            // TODO: blend
            double weftScale =-0.000000001 + (double) weft[0].length;
            int xWeft = px < 0.5 ? 0 : 1; // TODO fine-tune this breakpoint, poss. crop
            int yWeft = (int)(py * weftScale);
            pixels[i].setColor(weft[xWeft][yWeft]);

            // The high (yellow) end of the range is the high side of the rig
            // double c = py;
            // double d = pt.getY();
            // if(c < 0.5 && d < 0.0) pixels[i].setColor(0.0f,1.0f,0.0f);
            // else if(c >= 0.5 && d < 0.0) pixels[i].setColor(0.0f,1.0f,1.0f);
            // else if(c < 0.5 && d >= 0.0) pixels[i].setColor(1.0f,0.0f,0.0f);
            // else if(c >= 0.5 && d >= 0.0) pixels[i].setColor(1.0f,1.0f,0.0f);

            // the high (blue) end of the range is the larger (rear) gate in the rig
            // if(pt.getZ() - 5.0 < box.getMinZ()) pixels[i].setColor(1.0f,0.0f,0.0f); // front
            // if(pt.getZ() + 5.0 > box.getMaxZ()) pixels[i].setColor(0.0f,0.0f,1.0f); // back

            // The low (pink) end of the range is audience left
            // if(pt.getX() - 3.0 < box.getMinX()) pixels[i].setColor(1.0f,0.0f,1.0f); // left
            // if(pt.getX() + 3.0 > box.getMaxX()) pixels[i].setColor(0.0f,0.5f,1.0f); // right

        }
    }
}
