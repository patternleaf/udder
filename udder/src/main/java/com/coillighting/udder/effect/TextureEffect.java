package com.coillighting.udder.effect;

import java.awt.geom.Point2D;

import com.coillighting.udder.Device;
import com.coillighting.udder.Pixel;
import com.coillighting.udder.Stretch;
import com.coillighting.udder.TimePoint;
import com.coillighting.udder.geometry.BoundingCube;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

// TODO: LFO state for corners

public class TextureEffect extends EffectBase {

    protected String filename = null;
    protected BufferedImage image = null;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected BoundingCube deviceBounds = null;

    // Distortion quad for Stretch.stretchXY
    protected Point2D.Double sw = null;
    protected Point2D.Double se = null;
    protected Point2D.Double nw = null;
    protected Point2D.Double ne = null;

    public TextureEffect(String filename) {
        this.filename = filename;

        // Start with no distortion
        sw = new Point2D.Double(0.0, 0.0);
        se = new Point2D.Double(1.0, 0.0);
        nw = new Point2D.Double(0.0, 1.0);
        ne = new Point2D.Double(1.0, 1.0);

        this.reloadImage();
    }

    public Class getStateClass() {
        return String.class;
    }

    public Object getState() {
        return null; // TODO
    }

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
                    this.log("Error loading image " + filename);
                    this.log(iox);
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

    public void animate(TimePoint timePoint) {
        // TODO set .pixels given .devices' locations and contents of image file
        if(image == null) {
            for(Pixel px: pixels) {
                px.setColor(0.0f, 0.0f, 0.0f);
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
                xyNorm.x = (xyz[0] - devMinX) / devWidth;
                xyNorm.y = 1.0 - ((xyz[1] - devMinY) / devHeight);

                // Distort the image by stretching the flattened rig over it.
                Point2D.Double xyStretched = Stretch.stretchXY(xyNorm, sw, se, nw, ne);

                // TODO: bilinear interpolation instead of truncating coordinates here
                int imgX = (int)(xyStretched.x * imageWidth);
                int imgY = (int) (xyStretched.y * imageHeight);

                if(imgX < 0 || imgX >= imageWidth || imgY < 0 || imgY >= imageHeight) {
                    pixels[i].setColor(0.0f, 0.0f, 0.0f);
                } else {
                    int color = image.getRGB(imgX, imgY);
                    pixels[i].setRGBColor(color);
                }
            }
        }
    }

}
