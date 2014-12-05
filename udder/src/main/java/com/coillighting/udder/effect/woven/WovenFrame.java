package com.coillighting.udder.effect.woven;

import java.util.Random;

import com.coillighting.udder.blend.BlendOp;
import com.coillighting.udder.blend.MaxBlendOp;
import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.Point3D;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** A simple data structure by which a WovenEffect communicates pixels
 *  to its Cues.
 */
public class WovenFrame {

    protected int warpThreadcount = 16; // width is approx. double this
    protected int weftThreadcount = 32;
    protected BlendOp blendOp = null;
    protected double brightness = 1.0;

    /** A single pixel maps onto the whole background.
     * We previously used this to implement general washes and background
     * pulses, but those were cut 12/4, so now it's just a black value
     * for initializing this effect's internal compositing loop.
     */
    public Pixel background = null;

    /** A single horizontal scanline row represents the warp. */
    public Pixel[] warp = null; // [x]

    /** A pair of vertical scanline columns represent the weft. */
    public Pixel[][] weft = null; // [x][y]

    protected Random random = null;

    /** Array of [group0, group1] - whether to draw warp on this group. */
    boolean [] warpEnabled = {true, true};

    /** Array of [group0, group1] - whether to draw weft on this group. */
    boolean [] weftEnabled = {true, true};

    public WovenFrame() {
        this.random = new Random();
        this.blendOp = new MaxBlendOp();
        this.reset();
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
        this.randomizeLayerGrouping();
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

    /** The woven sculpture on the two gates are different in the orientation of
     *  their strongest lines. The front gate's weaving emphasizes vertical lines,
     *  and so the warp lights will run along its major axis. The rear gate's
     *  weaving emphasizes horizontal lines, and so the weft lights will emphasize
     *  the rear gate's major axis. Becky suggested that we break down the Woven
     *  effect sometimes, so 2/3 of the time, we show both warp and weft on both
     *  gates. The rest of the time we do something custom per gate. N.B. each
     *  gate is patched as its own group.
     */
    public void randomizeLayerGrouping() {
        // 2/3 of the time show everything everywhere.
        warpEnabled[0] = true;
        warpEnabled[1] = true;
        weftEnabled[0] = true;
        weftEnabled[1] = true;

        // Randomly switch to a breakdown mode for the other 1/3 of the time:
        final int randomMode = random.nextInt(101);
        if(randomMode > 90) {
            // Sometimes break it down between the two gates.
            if(randomMode < 99) {
                // About 30% of the time, show just warp in front, just weft in back.
                // This meshes with the woven sculpture's lines on those gates.
                warpEnabled[1] = false;
                weftEnabled[0] = false;
            } else {
                // Rarely, show a clashing scene whose light beams are mostly
                // perpendicular to the strong lines on the two gates.
                warpEnabled[0] = false;
                weftEnabled[1] = false;
            }
        }
    }

    /** Render this frame as ASCII art. Downsample and hex-encode colors. */
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
        // OPTIMIZATION CANDIDATE: compute these only when the device list changes
        final BoundingCube box = Device.getDeviceBoundingCube(devices);
        final double wScale = 1.0 / box.getWidth();
        final double hScale = 1.0 / box.getHeight();
        final double xOff = -box.getMinX();
        final double yOff = -box.getMinY();

        final double warpScale = -0.000000001 + (double) warp.length;
        final double weftScale = -0.000000001 + (double) weft[0].length;

        for(int i=0; i<devices.length; i++) {
            final Device device = devices[i];
            final int group = device.getGroup();

            if(group < 2) {

                final double px = wScale * (device.x + xOff); // normalized 0..1
                final double py = hScale * (device.y + yOff); // ditto

                // Draw the nearest neighbor in the warp for this pixel.
                // Fill from right to left.
                final int xWarp = warp.length - 1 - (int)(px * warpScale);

                // Draw the nearest neighbor in the weft for this pixel.
                final double center = 0.125; // oscillate around this line
                final int xWeft = px < center ? 0 : 1;
                final int yWeft = (int)(py * weftScale);

                final Pixel pixel = pixels[i];
                pixel.setColor(background);

                if(WovenFrame.isWarp(group, px, py)) {
                    if(warpEnabled[group]) {
                        pixel.blendWith(warp[xWarp], 1.0f, blendOp);
                    }
                } else if(weftEnabled[group]) {
                    pixel.blendWith(weft[xWeft][yWeft], 1.0f, blendOp);
                }
                pixel.scale((float)brightness);
            }
        }
    }

    /** Return whether a device at the given position should display warp
     *  (true) or weft (false) colors.
     */
    public static boolean isWarp(int group, double px, double py) {
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
            // At that point we'll need to restrict Woven's drawing to the two
            // target groups by returning null here.
            drawWarp = false;
        }

        return drawWarp;
    }
}
