package com.coillighting.udder.effect;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

import com.coillighting.udder.ControlQuad;
import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.Util;
import com.coillighting.udder.geometry.BoundingCube;

// TODO: LFO state for controlQuads

public class TextureEffect extends EffectBase {

    protected enum Interpolation {
        LINEAR, SINUSOIDAL, ROOT, POWER
    }

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

    /** The longest it will take for one corner to complete
     *  a single transit.
     */
    int maxTempoMillis = 18000;

    public TextureEffect(String filename) {
        this.filename = filename;
        random = new Random();
        controlQuadManual = new ControlQuad();
        controlQuadAutomaticStart = new ControlQuad();
        controlQuadAutomaticCurrent = new ControlQuad();
        controlQuadAutomaticTarget = new ControlQuad();
        transitTimesMillis = new long[4][2]; // defaults to 0s per java lang spec
        interpolations = new Interpolation[] { Interpolation.SINUSOIDAL,
                                               Interpolation.SINUSOIDAL,
                                               Interpolation.SINUSOIDAL,
                                               Interpolation.SINUSOIDAL };
        this.reloadImage();
    }

    public Class getStateClass() {
        return String.class;
    }

    public Object getState() {
        return null; // TODO
    }

    // TODO: quadmanual corners, mode
    public void setState(Object state) throws ClassCastException {
        String fn = (String) state;
        if(fn.equals("")) {
            this.filename = null;
        } else {
            this.filename = fn;
        }
        this.reloadImage();
    }

    public void patchDevices(List<Device> devices) {
        super.patchDevices(devices);
        deviceBounds = Device.getDeviceBoundingCube(devices.toArray(new Device[0]));
    }

    public void log(Object message) {
        // TODO proper logging
        System.out.println("" + message);
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
                this.log("File not found: " + filename);
                filename = null;
            } else if(!imageFile.isFile()) {
                this.log("Not a regular file: " + filename);
                filename = null;
            } else {
                try {
                    image = ImageIO.read(imageFile);
                } catch(IOException iox) {
                    this.log("Error loading image " + filename + "\n" + iox);
                    filename = null;
                    return;
                }
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                if(imageWidth == 0 || imageHeight == 0) {
                    this.log("Error loading " + filename + ": empty image.");
                    this.clearImage();
                    filename = null;
                }
            }
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
        transitTimesMillis[corner][1] = now + (long) random.nextInt(maxTempoMillis);
        pts.target.x = xmin + (xmax - xmin) * random.nextDouble();
        pts.target.y = ymin + (ymax - ymin) * random.nextDouble();

        pts.start.x = pts.current.x;
        pts.start.y = pts.current.y;

        this.randomizeInterpolationMode(corner);
    }

    private void randomizeInterpolationMode(int corner) {
        Interpolation mode;
        int r = random.nextInt(100);
        if(r < 10) {
            mode = Interpolation.LINEAR;
        } else if(r < 50) {
            mode = Interpolation.SINUSOIDAL;
        } else if(r < 80) {
            mode = Interpolation.ROOT;
        } else {
            mode = Interpolation.POWER;
        }
        interpolations[corner] = mode;
    }

    private void advance(int corner, long now) {
        Waypoints pts = this.getWaypoints(corner);
        // TODO: nonlinear interpo
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

        // TODO N and S are flipped due to flipped coordinates.
        // Figure out how to unflip them, or rename N and S. It's
        // not as simple as subtracting y from 1.0 here:

        Interpolation mode = interpolations[corner];
        if(mode == Interpolation.LINEAR) {
            pts.current.x = Util.crossfadeLinear(pct, pts.start.x, pts.target.x);
            pts.current.y = Util.crossfadeLinear(pct, pts.start.y, pts.target.y);
        } else if(mode == Interpolation.SINUSOIDAL) {
            pts.current.x = Util.crossfadeSinusoidal(pct, pts.start.x, pts.target.x);
            pts.current.y = Util.crossfadeSinusoidal(pct, pts.start.y, pts.target.y);
        } else if(mode == Interpolation.ROOT) {
            pts.current.x = Util.crossfadeExponential(pct, 0.5, pts.start.x, pts.target.x);
            pts.current.y = Util.crossfadeExponential(pct, 0.5, pts.start.y, pts.target.y);
        } else if(mode == Interpolation.POWER) {
            pts.current.x = Util.crossfadeExponential(pct, 2.5, pts.start.x, pts.target.x);
            pts.current.y = Util.crossfadeExponential(pct, 2.5, pts.start.y, pts.target.y);
        } else {
            throw new IllegalArgumentException("Unsupported interpolation (todo)" + interpolations[corner]);
        }
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
        // TODO set .pixels given .devices' locations and contents of image file
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
                xyNorm.x = (xyz[0] - devMinX) / devWidth;
                xyNorm.y = 1.0 - ((xyz[1] - devMinY) / devHeight);

                // Distort the image by stretching the flattened rig over it.
                Point2D.Double xyStretched = controlQuad.stretchXY(xyNorm);

                // TODO: bilinear interpolation instead of truncating coordinates here
                int imgX = (int)(xyStretched.x * imageWidth);
                int imgY = (int) (xyStretched.y * imageHeight);

                if(imgX < 0 || imgX >= imageWidth || imgY < 0 || imgY >= imageHeight) {
                    pixels[i].setBlack();
                } else {
                    int color = image.getRGB(imgX, imgY);
                    pixels[i].setRGBColor(color);
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
