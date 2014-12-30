package com.coillighting.udder.effect;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.Bilinear;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

import static com.coillighting.udder.util.LogUtil.log;


/** Roll a raster over the devices in your show. The image wraps around at the
 * edges. You may roll horizontally and/or vertically. See RollEffectState for
 * options.
 */
public class RollEffect extends EffectBase {

    protected String filename = null;
    protected BufferedImage image = null;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected BoundingCube deviceBounds = null;

    /** offset. 0.0 = no rotation, 1.0 = 100% rotation (equivalent to 0.0). */
    protected double xRotate = 0.0;
    protected double yRotate = 0.0;

    /** <=0 = halt rotation, 1000 = 1 second */
    protected int xPeriodMillis = 0;
    protected int yPeriodMillis = 0;
    // TODO probably want to initialize this to stationary

    /** scene time when we last began rolling the image along the x-axis */
    protected long xStartTimeMillis = 0;
    protected long yStartTimeMillis = 0;

    /** scene time of last frame rendered */
    protected long currentTimeMillis = 0;

    protected boolean interpolateBilinear = true;

    protected Bilinear bilinear;

    // Scratch variables that we shouldn't reallocate on every
    // trip through the animation loop:
    private Point2D.Double xyNorm;

    public RollEffect(String filename) {
        this.filename = filename;
        bilinear = new Bilinear();

        // Initialize temps
        xyNorm = new Point2D.Double(0.0, 0.0);

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

        this.setXRotate(command.getXRotate());
        this.setYRotate(command.getYRotate());

        // Set period after rotate because we can skip time offset recalculation
        // if the rotate setters already reinitialized *StartTimeMillis.
        this.setXPeriodMillis(command.getXPeriodMillis());
        this.setYPeriodMillis(command.getYPeriodMillis());

        String fn = command.getFilename();
        if(!(fn == null || fn.equals("") || fn.equals(filename))) {
            filename = fn;

            // Reload last in case there is a problem reading the file.
            this.reloadImage();
        }
    }

    /** Leave the current value unchanged if xr is null. */
    public void setXRotate(Double xr) {
        if(xr != null) {
            double xrd = xr % 1.0;
            if(xrd < 0.0) xrd += 1.0;
            xRotate = xrd;

            // Reset image position whenever x is explicitly set.
            xStartTimeMillis = 0;
        }
    }

    /** Leave the current value unchanged if yr is null. */
    public void setYRotate(Double yr) {
        if(yr != null) {
            double yrd = yr % 1.0;
            if(yrd < 0.0) yrd += 1.0;
            yRotate = yrd;

            // Reset image position whenever y is explicitly set.
            yStartTimeMillis = 0;
        }
    }

    /** Leave the current value unchanged if xmillis is null.
     * Calculate current phase offset and correct the x-timing
     * for smooth modulation of frequency. Otherwise every
     * adjustment to horizontal period would glitch the phase.
     */
    public void setXPeriodMillis(Integer xmillis) {
        if(xmillis != null) {
            int xmi = (int) xmillis;
            if(xmi != xPeriodMillis) {
                if(xStartTimeMillis > 0 && currentTimeMillis > xStartTimeMillis) {
                    double elapsed = (((double) (currentTimeMillis - xStartTimeMillis))
                            / (double) xPeriodMillis) % 1.0;
                    double reclockedElapsed = elapsed * (double) xmi;
                    xStartTimeMillis = currentTimeMillis - (long) reclockedElapsed;
                }
                xPeriodMillis = xmi;
            }
        }
    }

    /** Leave the current value unchanged if ymillis is null.
     * Calculate current phase offset and correct the y-timing
     * for smooth modulation of frequency. Otherwise every
     * adjustment to vertical period would glitch the phase.
     */
    public void setYPeriodMillis(Integer ymillis) {
        if(ymillis != null) {
            int ymi = (int) ymillis;
            if(ymi != yPeriodMillis) {
                if(yStartTimeMillis > 0 && currentTimeMillis > yStartTimeMillis) {
                    double elapsed = (((double) (currentTimeMillis - yStartTimeMillis))
                            / (double) yPeriodMillis) % 1.0;
                    double reclockedElapsed = elapsed * (double)ymi;
                    yStartTimeMillis = currentTimeMillis - (long) reclockedElapsed;
                }
                yPeriodMillis = ymi;
            }
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
            xyNorm.setLocation(0.0, 0.0);

            long now = timePoint.sceneTimeMillis();

            // Frame offsets given baseline x/y rotation and timepoint in the cycle,
            // NOT YET normalized to [0.0..1.0) -- that happens below.
            double xFrameOffset;
            double yFrameOffset;

            if(xStartTimeMillis <= 0) {
                xFrameOffset = 0.0;
                xStartTimeMillis = now;
            } else {
                long xElapsed = now - xStartTimeMillis;
                xFrameOffset = xRotate;
                if(xPeriodMillis != 0) {
                    // Subtract so that a positive period scrolls image details
                    // to the right across a conventionally oriented rig.
                    xFrameOffset -= (double) xElapsed / (double) xPeriodMillis;
                }
            }

            if(yStartTimeMillis <= 0) {
                yFrameOffset = 0.0;
                yStartTimeMillis = now;
            } else {
                long yElapsed = now - yStartTimeMillis;
                yFrameOffset = yRotate;
                if(yPeriodMillis != 0) {
                    // Add so that a positive period scrolls image details
                    // upward across a conventionally oriented rig (because image
                    // coordinates are flipped, with a NW origin).
                    yFrameOffset += (double) yElapsed / (double) yPeriodMillis;
                }
            }

            currentTimeMillis = now;

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

                double x = (xFrameOffset + xyNorm.x) % 1.0;
                if(x < 0.0) {
                    x += 1.0;
                }

                double y = (yFrameOffset + xyNorm.y) % 1.0;
                if(y < 0.0) {
                    y += 1.0;
                }

                if(!interpolateBilinear) {
                    // Truncate mode: round down fractions to an integer pixel
                    // coordinate, and send that pixel's color to this device
                    // point. Low-rez but a good reference for debugging the
                    // bilinear mode. Wrap around at the edges. Normalize to
                    // [0.0..1.0).
                    int imgX = ((int) (x * imageWidth));
                    int imgY = ((int) (y * imageHeight));

                    try {
                        int color = image.getRGB(imgX, imgY);
                        pixels[i].setRGBColor(color);
                    } catch(ArrayIndexOutOfBoundsException e) {
                        pixels[i].setBlack();
                    }

                } else {
                    bilinear.interpolate(pixels[i], image, imageWidth, imageHeight, x, y, false);
                }
            }
        }
    }
}
