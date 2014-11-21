package com.coillighting.udder.effect;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.ControlQuad;
import com.coillighting.udder.geometry.Interpolator;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

import static com.coillighting.udder.geometry.Interpolator.Interpolation;
import static com.coillighting.udder.util.LogUtil.log;


/** Stretch and squeeze a raster image over the pointcloud representing the
 *  Devices in your show. See TextureEffectState for options.
 *
 *  FUTURE add an affine transform layer to the manual side, for
 *  easy rotations, scalings, etc.
 */
public class TextureEffect extends EffectBase {

    protected Interpolator interpolator = null;
    protected Random random = null;

    protected String filename = null;
    protected BufferedImage image = null;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected BoundingCube deviceBounds = null;
    protected ControlQuad controlQuadManual = null;
    protected ControlQuad controlQuadAutomaticStart = null;
    protected ControlQuad controlQuadAutomaticCurrent = null;
    protected ControlQuad controlQuadAutomaticTarget = null;

    /** [start, end] in ControlQuad order: sw, se, nw, ne. */
    protected long[][] transitTimesMillis = null;
    protected Interpolation[] interpolations = null;
    protected boolean automatic = true;
    protected boolean interpolateBilinear = true;

    // Scratch variables that we shouldn't reallocate on every
    // trip through the animation loop:
    private Pixel p, p11, p12, p21, p22;

    /** The longest it will take for one corner to complete
     *  a single transit.
     */
    int maxTempoMillis = 18000;

    public TextureEffect(String filename) {
        this.filename = filename;
        random = new Random();
        interpolator = new Interpolator();
        controlQuadManual = new ControlQuad();
        controlQuadAutomaticStart = new ControlQuad();
        controlQuadAutomaticCurrent = new ControlQuad();
        controlQuadAutomaticTarget = new ControlQuad();
        transitTimesMillis = new long[4][2]; // defaults to 0s per java lang spec
        interpolations = new Interpolation[] {
            Interpolation.SINUSOIDAL,
            Interpolation.SINUSOIDAL,
            Interpolation.SINUSOIDAL,
            Interpolation.SINUSOIDAL
        };

        // Initialize temps
        p = Pixel.black();
        p11 = Pixel.black();
        p12 = Pixel.black();
        p21 = Pixel.black();
        p22 = Pixel.black();

        this.reloadImage();
    }

    public Class getStateClass() {
        return TextureEffectState.class;
    }

    public Object getState() {
        return null; // TODO TextureEffectState
    }

    public void setState(Object state) throws ClassCastException {
        TextureEffectState command = (TextureEffectState) state;

        automatic = command.getAutomatic();

        int tempo = command.getMaxTempoMillis();
        if(tempo >= 1) {
            maxTempoMillis = tempo;
        }

        ControlQuad quad = command.getControlQuad();
        if(quad != null) {
            // Ensure that the resulting control polygon remains valid.
            controlQuadManual.setDoubleValues(quad);
        }

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
            log("TextureEffect: no image to load.");
        }
    }

    private Waypoints getWaypoints(int corner) {
        Waypoints pts = new Waypoints();
        if(corner == Waypoints.SW) {
            pts.start = controlQuadAutomaticStart.sw;
            pts.current = controlQuadAutomaticCurrent.sw;
            pts.target = controlQuadAutomaticTarget.sw;
        } else if(corner == Waypoints.SE) {
            pts.start = controlQuadAutomaticStart.se;
            pts.current = controlQuadAutomaticCurrent.se;
            pts.target = controlQuadAutomaticTarget.se;
        } else if(corner == Waypoints.NW) {
            pts.start = controlQuadAutomaticStart.nw;
            pts.current = controlQuadAutomaticCurrent.nw;
            pts.target = controlQuadAutomaticTarget.nw;
        } else if(corner == Waypoints.NE) {
            pts.start = controlQuadAutomaticStart.ne;
            pts.current = controlQuadAutomaticCurrent.ne;
            pts.target = controlQuadAutomaticTarget.ne;
        } else {
            throw new IllegalArgumentException("Unknown waypoint corner " + corner);
        }
        return pts;
    }

    /** Arrive at the destination specified in targetPoint, then
     *  calculate a new destination and begin a new transit.
     */
    private void randomizeDestination(int corner, long now,
                                      double xmin, double xmax,
                                      double ymin, double ymax)
    {
        Waypoints pts = this.getWaypoints(corner);

        // Complete the current transit.
        pts.current.x = pts.target.x;
        pts.current.y = pts.target.y;

        // Randomly select the next dest and start progress.
        transitTimesMillis[corner][0] = now;
        long nextDuration = (long) random.nextInt(maxTempoMillis);
        if(nextDuration < 1) {
            nextDuration = 1;
        }
        transitTimesMillis[corner][1] = now + nextDuration;
        pts.target.x = xmin + (xmax - xmin) * random.nextDouble();
        pts.target.y = ymin + (ymax - ymin) * random.nextDouble();

        pts.start.x = pts.current.x;
        pts.start.y = pts.current.y;

        this.randomizeInterpolationMode(corner);
    }

    private void randomizeInterpolationMode(int corner) {
        interpolations[corner] = interpolator.randomMode(10, 50, 80);
    }

    private void advance(int corner, long now) {
        Waypoints pts = this.getWaypoints(corner);
        long began = transitTimesMillis[corner][0];
        long eta = transitTimesMillis[corner][1];
        long duration = eta - began;
        long progress = now - began;

        // Convert percent elapsed to distance.
        double pct = (double) progress / duration;
        if(pct > 1.0) {
            pct = 1.0;
        } else if(pct < 0.0) {
            pct = 0.0;
        }

        // FIXME N and S are flipped due to flipped coordinates.
        // Figure out how to unflip them, or rename N and S. It's
        // not as simple as subtracting y from 1.0 here:

        Interpolation mode = interpolations[corner];
        interpolator.interpolate2D(mode, pct, pts.start, pts.current, pts.target);
    }

    private void animateCorner(int corner, long now,
                          double xmin, double xmax,
                          double ymin, double ymax)
    {
        if(now >= transitTimesMillis[corner][1]) {
            this.randomizeDestination(corner, now, xmin, xmax, ymin, ymax);
        } else {
            this.advance(corner, now);
        }

    }

    public void animateControls(TimePoint timePoint) {
        Point2D.Double pt;
        double xmax, xmin, ymax, ymin;
        long now = timePoint.sceneTimeMillis();
        this.animateCorner(Waypoints.SW, now, 0.0, 0.5, 0.0, 0.5);
        this.animateCorner(Waypoints.SE, now, 0.5, 1.0, 0.0, 0.5);
        this.animateCorner(Waypoints.NW, now, 0.0, 0.5, 0.5, 1.0);
        this.animateCorner(Waypoints.NE, now, 0.5, 1.0, 0.5, 1.0);
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
            ControlQuad controlQuad;

            if(!automatic) {
                controlQuad = controlQuadManual;
            } else {
                this.animateControls(timePoint);
                controlQuad = controlQuadAutomaticCurrent;
            }

            for(int i=0; i<devices.length; i++) {
                Device dev = devices[i];
                double[] xyz = dev.getPoint();

                // Flatten the rig and normalize this Device's location
                // in space to a coordinate inside the unit square.
                // Also flip the Y axis so that the image is right side up
                // when projected on the rig.
                // FUTURE: distort the mapping in 3D, not just 2D. We can
                // get away with 2D at the Dairy because the two rigs aren't
                // quite the same size and shape, but a Cubatron-like setup
                // deserves full 3D texture mapping.
                xyNorm.x = (xyz[0] - devMinX) / devWidth;
                xyNorm.y = 1.0 - ((xyz[1] - devMinY) / devHeight);

                // Distort the image by stretching the flattened rig over it.
                Point2D.Double xyStretched = controlQuad.stretchXY(xyNorm);

                if(!interpolateBilinear) {
                    // Truncate mode: round down fractions to an integer pixel
                    // coordinate, and send that pixel's color to this device
                    // point. Low-rez but a good reference for debugging the
                    // bilinear mode.

                    int imgX = ((int) (xyStretched.x * imageWidth) - 1);
                    int imgY = ((int) (xyStretched.y * imageHeight) - 1);
                    if(imgX < 0 || imgX >= imageWidth || imgY < 0 || imgY >= imageHeight) {
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
                    final boolean streakEnabled = false;
                    boolean streaked = false;
                    double x = (xyStretched.x * imageWidth) - 1;
                    if(x < 0.0) {
                        // This will streak the edges if coordinates are out of bounds.
                        x = 0.0;
                        streaked = true;
                    } else if(x >= imageWidth) {
                        x = imageWidth - 1; // streak
                        streaked = true;
                    }
                    int x1 = (int) Math.floor(x);
                    int x2 = (int) Math.ceil(x);
                    if(x2 >= imageWidth) {
                        x2 = imageWidth - 1; // streak
                        streaked = true;
                    }

                    double y = (xyStretched.y * imageHeight) - 1;
                    if(y < 0.0) {
                        y = 0.0; // streak
                        streaked = true;
                    } else if(y >= imageHeight) {
                        y = imageHeight - 1; // streak
                        streaked = true;
                    }
                    int y1 = (int) Math.floor(y);
                    int y2 = (int) Math.ceil(y);
                    if(y2 >= imageHeight) {
                        y2 = imageHeight - 1; // streak
                        streaked = true;
                    }

                    if(streaked && ! streakEnabled) {
                        pixels[i].setBlack();
                    } else {
                        // Sample colors from the four surrounding pixels.
                        p11.setRGBColor(image.getRGB(x1, y1));
                        p21.setRGBColor(image.getRGB(x2, y1));
                        p12.setRGBColor(image.getRGB(x1, y2));
                        p22.setRGBColor(image.getRGB(x2, y2));

                        // First we do two linear interpolations, R1 and R2,
                        // in the x direction.
                        final double right = (x1 == x2 ? 0.0 : (x2 - x) / (x2 - x1));
                        final double left = (x1 == x2 ? 1.0 : (x - x1) / (x2 - x1));

                        final double rR1 = p11.r * right + p21.r * left;
                        final double gR1 = p11.g * right + p21.g * left;
                        final double bR1 = p11.b * right + p21.b * left;

                        final double rR2 = p12.r * right + p22.r * left;
                        final double gR2 = p12.g * right + p22.g * left;
                        final double bR2 = p12.b * right + p22.b * left;

                        // Next interpolate R1 and R2 in the Y direction.
                        final double high = (y1 == y2 ? 0.0 : (y2 - y) / (y2 - y1));
                        final double low = (y1 == y2 ? 1.0 : (y - y1) / (y2 - y1));
                        p.r = (float) (rR1 * high + rR2 * low);
                        p.g = (float) (gR1 * high + gR2 * low);
                        p.b = (float) (bR1 * high + bR2 * low);

                        pixels[i].setColor(p);
                        // Of course, this whole time we have falsely assumed
                        // linear gamma.
                    }
                }
            }
        }
    }

    public boolean getAutomatic() {
        return this.automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }
}


class Waypoints {
    public final static int SW = 0;
    public final static int SE = 1;
    public final static int NW = 2;
    public final static int NE = 3;

    public Point2D.Double start = null;
    public Point2D.Double current = null;
    public Point2D.Double target = null;
}
