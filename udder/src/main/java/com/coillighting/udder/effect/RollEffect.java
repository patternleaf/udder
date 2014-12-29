package com.coillighting.udder.effect;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//import java.util.List;
//import java.util.Random;
import javax.imageio.ImageIO;
//
import com.coillighting.udder.geometry.BoundingCube;
//import com.coillighting.udder.geometry.ControlQuad;
import com.coillighting.udder.geometry.Interpolator;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

import static com.coillighting.udder.util.LogUtil.log;


/** Roll a raster over the devices in your show. The image wraps around at the
 * edges. You may roll horizontally and/or vertically. See RollEffectState for
 * options.
 */
public class RollEffect extends EffectBase {

    protected Interpolator interpolator = null;

    protected String filename = null;
    protected BufferedImage image = null;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected BoundingCube deviceBounds = null;

    /** 0.0 = no rotation, 1.0 = 100% rotation (equivalent to 0.0). */
    protected double xRotate = 0.0;
    protected double yRotate = 0.0;

    /** <=0 = halt rotation, 1000 = 1 second */
    protected int xPeriodMillis = 1000;
    protected int yPeriodMillis = 1000;

    protected boolean interpolateBilinear = false;

    // TODO
    // Scratch variables that we shouldn't reallocate on every
    // trip through the animation loop:
    // private Pixel p, p11, p12, p21, p22;

    public RollEffect(String filename) {
        this.filename = filename;
        interpolator = new Interpolator();
        this.reloadImage();
    }

    public Class getStateClass() {
        return RollEffectState.class;
    }

    public Object getState() {
        return null; // TODO RollEffectState
    }

    public void setState(Object state) throws ClassCastException {
        RollEffectState command = (RollEffectState) state;

        // TODO figure out how to vary rate without changing offset. might
        // need to use Doubles and Integers rather than ints and doubles.

        int xmillis = command.getXPeriodMillis();
        xPeriodMillis = xmillis <= 0 ? 0 : xmillis;

        int ymillis = command.getYPeriodMillis();
        yPeriodMillis = ymillis <= 0 ? 0 : ymillis;

        double xrotate = command.getXRotate() % 1.0;
        if(xrotate < 0.0) xrotate += 1.0;
        xRotate = xrotate;

        double yrotate = command.getYRotate() % 1.0;
        if(yrotate < 0.0) yrotate += 1.0;
        yRotate = yrotate;

        String fn = command.getFilename();
        if(!(fn == null || fn.equals("") || fn.equals(filename))) {
            filename = fn;

            // Reload last in case there is a problem reading the file.
            this.reloadImage();
        }
    }

    public void patchDevices(Device[] devices) {
        super.patchDevices(devices);
        deviceBounds = Device.getDeviceBoundingCube(devices);
    }

    private void clearImage() {
        image = null;
        imageWidth = 0;
        imageHeight = 0;
    }

    /** If we can't load the image, log an error and proceed.
     *  Don't crash the server.
     *  FIXME REF deduplicate w/TextureEffect.reloadImage
     */
    public void reloadImage() {
        this.clearImage();
        if(filename != null) {
            File imageFile = new File(filename);
            if(!imageFile.exists()) {
                log("File not found: " + filename);
                filename = null;
            } else if(!imageFile.isFile()) {
                log("Not a regular file: " + filename);
                filename = null;
            } else {
                try {
                    image = ImageIO.read(imageFile);
                } catch(IOException iox) {
                    log("Error loading image " + filename + "\n" + iox);
                    filename = null;
                    return;
                }
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                if(imageWidth == 0 || imageHeight == 0) {
                    log("Error loading " + filename + ": empty image.");
                    this.clearImage();
                    filename = null;
                }
            }
        } else {
            log("RollEffect: no image to load.");
        }
    }

    public void animate(TimePoint timePoint) {
        if(image == null) {
            for(Pixel px: pixels) {
                px.setBlack();
            }
        } else {
            final double devMinX = deviceBounds.getMinX();
            final double devMinY = deviceBounds.getMinY();
            final double devWidth = deviceBounds.getWidth();
            final double devHeight = deviceBounds.getHeight();
            Point2D.Double xyNorm = new Point2D.Double(0.0, 0.0);

            for(int i=0; i<devices.length; i++) {
                Device dev = devices[i];
                double[] xyz = dev.getPoint();

                // Flatten the rig and normalize this Device's location
                // in space to a coordinate inside the unit square.
                // Also flip the Y axis so that the image is right side up
                // when projected on the rig.
                // FUTURE: option to 3D rotate the virtual projector position
                // so we can roll the image in 2D while transforming the
                // axis of extrusion in 3D.
                xyNorm.x = (xyz[0] - devMinX) / devWidth;
                xyNorm.y = 1.0 - ((xyz[1] - devMinY) / devHeight);

                if(!interpolateBilinear) {
                    // Truncate mode: round down fractions to an integer pixel
                    // coordinate, and send that pixel's color to this device
                    // point. Low-rez but a good reference for debugging the
                    // bilinear mode. Wrap around at the edges.

                    double x = (xRotate + xyNorm.x) % 1.0;
                    if(x < 0.0) {
                        x += 1.0;
                    }

                    double y = (yRotate + xyNorm.y) % 1.0;
                    if(y < 0.0) {
                        y += 1.0;
                    }

                    int imgX = ((int) (x * imageWidth));
                    int imgY = ((int) (y * imageHeight));

                    if(imgX < 0 || imgX >= imageWidth || imgY < 0 || imgY >= imageHeight) {
                        // SANITY. TODO: which exception will image.getRGB throw when out of bounds?
                        pixels[i].setBlack();
                    } else {
                        int color = image.getRGB(imgX, imgY);
                        pixels[i].setRGBColor(color);
                    }

                } else {
                    // Bilinear (quadratic) interpolation mode: given the four
                    // closest pixels to this device point, compute its color.
                    // https://en.wikipedia.org/wiki/Bilinear_interpolation

                    // Decide whether to clip out-of-bounds pixel coordinates
                    // to the edge colors (causing streaking, sometimes looks
                    // good) or just crop them (color that part of the rig
                    // black). false=crop. FUTURE: could export this option.


//                    final boolean streakEnabled = false;
//                    boolean streaked = false;
//                    double x = (xyStretched.x * imageWidth) - 1;
//                    if(x < 0.0) {
//                        // This will streak the edges if coordinates are out of bounds.
//                        x = 0.0;
//                        streaked = true;
//                    } else if(x >= imageWidth) {
//                        x = imageWidth - 1; // streak
//                        streaked = true;
//                    }
//                    int x1 = (int) Math.floor(x);
//                    int x2 = (int) Math.ceil(x);
//                    if(x2 >= imageWidth) {
//                        x2 = imageWidth - 1; // streak
//                        streaked = true;
//                    }
//
//                    double y = (xyStretched.y * imageHeight) - 1;
//                    if(y < 0.0) {
//                        y = 0.0; // streak
//                        streaked = true;
//                    } else if(y >= imageHeight) {
//                        y = imageHeight - 1; // streak
//                        streaked = true;
//                    }
//                    int y1 = (int) Math.floor(y);
//                    int y2 = (int) Math.ceil(y);
//                    if(y2 >= imageHeight) {
//                        y2 = imageHeight - 1; // streak
//                        streaked = true;
//                    }
//
//                    if(streaked && ! streakEnabled) {
//                        pixels[i].setBlack();
//                    } else {
//                        // Sample colors from the four surrounding pixels.
//                        p11.setRGBColor(image.getRGB(x1, y1));
//                        p21.setRGBColor(image.getRGB(x2, y1));
//                        p12.setRGBColor(image.getRGB(x1, y2));
//                        p22.setRGBColor(image.getRGB(x2, y2));
//
//                        // First we do two linear interpolations, R1 and R2,
//                        // in the x direction.
//                        final double right = (x1 == x2 ? 0.0 : (x2 - x) / (x2 - x1));
//                        final double left = (x1 == x2 ? 1.0 : (x - x1) / (x2 - x1));
//
//                        final double rR1 = p11.r * right + p21.r * left;
//                        final double gR1 = p11.g * right + p21.g * left;
//                        final double bR1 = p11.b * right + p21.b * left;
//
//                        final double rR2 = p12.r * right + p22.r * left;
//                        final double gR2 = p12.g * right + p22.g * left;
//                        final double bR2 = p12.b * right + p22.b * left;
//
//                        // Next interpolate R1 and R2 in the Y direction.
//                        final double high = (y1 == y2 ? 0.0 : (y2 - y) / (y2 - y1));
//                        final double low = (y1 == y2 ? 1.0 : (y - y1) / (y2 - y1));
//                        p.r = (float) (rR1 * high + rR2 * low);
//                        p.g = (float) (gR1 * high + gR2 * low);
//                        p.b = (float) (bR1 * high + bR2 * low);
//
//                        pixels[i].setColor(p);
//                        // Of course, this whole time we have falsely assumed
//                        // linear gamma.
//                    }
                }
            }
        }
    }
}
