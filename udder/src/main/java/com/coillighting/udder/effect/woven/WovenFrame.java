package com.coillighting.udder.effect.woven;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.Point3D;

import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;

/** A simple, public data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int warpThreadcount = 16; // width is approx. double this
    protected int weftThreadcount = 32;
    protected BlendOp blendOp = null;
    protected double brightness = 1.0;

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

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    /** Without reallocating pixels, set all colors to a uniform value. */
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

    /** Render this frame as ASCII art.
     *  Downsample and hex-encode color values.
     */
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
        BoundingCube box = Device.getDeviceBoundingCube(devices);
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

            // TEMP background colors to indicate px and group# (green=front)
            // these actually look pretty good as the background for a high sat
            // blue warp and a high sat red weft:
            // if(group == 0) pixel.setColor(0.0f, 0.7f * (float) px, 0.0f);
            // else if(group == 1) pixel.setColor(0.7f * (float) px, 0.0f, 0.0f);

            // Draw the nearest neighbor in the weft for this pixel.
            double weftScale =-0.000000001 + (double) weft[0].length;
            double center = 0.125;
            int xWeft = px < center ? 0 : 1; // TODO fine-tune this breakpoint, poss. crop
            int yWeft = (int)(py * weftScale);

            // Mask out some unsightly areas.
            // Northeast overhang: x .97-.98, y .9-.98

            // TODO Refactor to encode masks as BoundingCubes for clarity.

            boolean drawWarp = true;

            if(px > 0.83 && py > 0.33 && py < 0.996) {
                // Crop right edge ascenders out of the warp...
                if(group == 1) {
                    // ...but don't omit the rear gate's northeast peak.
                    if(!(px <0.95 && py >0.99)) {
                        drawWarp = false;
                    }
                } else if(py < 0.8) { // group 0
                    // ...and watch out for the tight corner in the front gate's
                    // northeastern region. (There is a second, small ascender.)
                    if (px < 0.89 || px > 0.91) {
                        drawWarp = false;
                    }
                }
            } else if(group == 0) {
                // Crop southwest ascenders for the front gate
                if(px < 0.268 && py < 0.39) {
                    // FYI 0.23 is the top of the right SW ascender
                    drawWarp = false;
                }
            } else if(group == 1) {
                // Crop southwest ascenders for the rear gate
                if(px < 0.7 && py < 0.4) {
                    // left ascender
                    drawWarp = false;
                } else if(px > 0.25 && px < 0.365 && py < 0.42) {
                    // right ascender
                    drawWarp = false;
                }
            } else {
                // Group > 1 isn't currently defined, but somebody might add it.
                // Restrict Woven's drawing to the two target groups.
                drawWarp = false;
            }

            pixel.setColor(background);

            if(drawWarp) {
                pixel.blendWith(warp[xWarp], 1.0f, blendOp);
            } else {
                pixel.blendWith(weft[xWeft][yWeft], 1.0f, blendOp);
            }

            pixel.scale((float)brightness);
        }
    }
}
