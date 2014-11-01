package com.coillighting.udder.effect.woven;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;

/** A simple, public data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int warpThreadcount = 32; // width is approx. double this
    protected int weftThreadcount = 32;
    protected BlendOp blendOp = null;

    /** A single pixel maps onto the whole background. */
    public Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
    public Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
    public Pixel[][] weft = null; // [x][y]

    public WovenFrame() {
        this.reset();
        this.blendOp = new MaxBlendOp();
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
        for(Pixel p: warp) {
            p.setColor(color);
        }
        for(Pixel[] column: weft) {
            for(Pixel p: column) {
                p.setColor(color);
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

        for(Pixel p: warp) {
            sb.append(p.toHexRGB()).append(' ');
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
            Pixel pixel = pixels[i];
            Device device = devices[i];
            int group = device.getGroup();

            // Scale this pixel's device coordinates into the unit square.
            Point3D pt = device.getPoint3D();

            double px = wScale * (pt.getX() + xOff); // normalized 0..1
            double py = hScale * (pt.getY() + yOff); // ditto
            // TODO account for z or group?

            // System.err.println("px " + px + " = wScale " + wScale + " * (pt.getX() " + pt.getX() + " + xOff " + xOff + ") warpLen=" + warp.length);

            // Draw the nearest neighbor in the warp for this pixel.
            double warpScale = -0.000000001 + (double) warp.length;

            // Fill from right to left.
            int xWarp = warp.length - 1 - (int)(px * warpScale);

            pixel.setColor(background);

            // Crop out some unsightly areas.

            //northeast overhang:
            // y .9-.98
            // x .97-.98

            boolean drawWarp = true;

            // Crop right edge ascenders out of the warp (both front and back gates).
            if(px > 0.83 && px < 0.995 && py > 0.36 && py < 0.997) {
                drawWarp = false;
            } else if(group == 0) {
                // Crop southwest ascenders for the front gate
//                if(px < 0.01 /*&& py < 0.39*/) {
//                    drawWarp = false;
//                }
            } else if(group == 1) {
                drawWarp = false;
                // Crop southwest ascenders for the rear gate
                if(px < 0.07 && py < 0.4) {
                    drawWarp = false;
                }
            }

            // TEMP background colors to indicate px and group# (green=front)
            if(group == 0) pixel.setColor(0.0f, 0.7f * (float) px, 0.0f);
            else if(group == 1) pixel.setColor(0.7f * (float) px, 0.0f, 0.0f);

            if(drawWarp) {
                pixel.blendWith(warp[xWarp], 1.0f, blendOp);
            }
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
            double weftScale =-0.000000001 + (double) weft[0].length;
            double center = 0.125;
            int xWeft = px < center ? 0 : 1; // TODO fine-tune this breakpoint, poss. crop
            int yWeft = (int)(py * weftScale);

            pixel.blendWith(weft[xWeft][yWeft], 1.0f, blendOp);
            pixel.blendWith(background, 1.0f, blendOp);


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
