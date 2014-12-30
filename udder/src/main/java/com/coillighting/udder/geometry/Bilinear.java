package com.coillighting.udder.geometry;

import java.awt.image.BufferedImage;

import com.coillighting.udder.model.Pixel;

// TODO figure out how many of these 'finals' are required to cause inlining
public final class Bilinear {

    // Scratch variables that we shouldn't reallocate on every
    // trip through the animation loop:
    private Pixel p, p11, p12, p21, p22;

    public Bilinear() {
        // Initialize temps
        p = Pixel.black();
        p11 = Pixel.black();
        p12 = Pixel.black();
        p21 = Pixel.black();
        p22 = Pixel.black();
    }

    /** Bilinear (quadratic) interpolation mode: given the four
     * closest pixels to this device point, compute its color.
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     *
     * Falsely assume linear gamma.
     *
     * image - Interpolate pixel values at coordinates lying on or between
     * pixels from this RGB image.
     *
     * imageWidth, imageHeight - Avoid repeatedly querying image's dimensions
     * in a hot loop by requiring the user to determine them ahead of time.
     * This also means that you may use a proper subset of the image by setting
     * these values to something less than its full dimensions. These values
     * are not checked against the given image, so GIGO.
     *
     * xNormalized, yNormalized - Interpolate the value at this
     * location, expressed as a fraction of the given image's width and height.
     * The image's center point, for example, is (0.5, 0.5).
     *
     * outputPixel - Write the interpolated value into this Pixel's value.
     *
     * streakEnabled - Decide whether to clip out-of-bounds pixel coordinates
     * to the edge colors (causing streaking, which sometimes looks good) or
     * just crop them (color that part of the rig black). false=crop.
     */
    public final void interpolate(
        Pixel outputPixel, BufferedImage image, int imageWidth, int imageHeight,
        double xNormalized, double yNormalized, boolean streakEnabled)
    {
        boolean streaked = false;
        double x = (xNormalized * imageWidth) - 1;
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

        double y = (yNormalized * imageHeight) - 1;
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
            outputPixel.setBlack();
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

            outputPixel.setColor(p);
        }

    }

}