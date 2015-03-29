package com.coillighting.udder.effect;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.TriangularSequence;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

import static com.coillighting.udder.util.LogUtil.log;

/** TODO DOC */
public class BloomEffect extends EffectBase {

    /** Checkerboard (traditional for Blooming Leaf weaves). */
    public static final int DEFAULT_PALETTE_SIZE = 2;
    protected int[] repertoire = BloomTiling.REPERTOIRES[DEFAULT_PALETTE_SIZE];
    protected Pixel[] palette = {Pixel.white(), Pixel.black()};
    protected int[][] tiling = BloomTiling.TILINGS_2D[repertoire.length];

    // TODO parameterize scale modulation?
    // scale: device space units per thread
    protected double scale = 1.0;
    protected double scaleIncrement = 0.05;

    /** Reflect the effect down the middle. */
    protected boolean enableBilateralSym = true;

    /** Reflect the reflection. (Do nothing if enableBilateralSym is false.)
     *  Traditional for Blooming Leaves.
     */
    protected boolean enableNestedBilateralSym = true;

    protected BoundingCube deviceBounds = null;

    protected boolean enableX = true;
    protected double devMinX = 0.0;
    protected double devWidth = 0.0;
    protected double xCenterOffset = 0.0;
    protected double xQuarterOffset = 0.0;

    protected boolean enableY = true;
    protected double devMinY = 0.0;
    protected double devHeight = 0.0;
    protected double yCenterOffset = 0.0;
    protected double yQuarterOffset = 0.0;

    // Enabling Z+reflection at the Dairy, which consists of two parallel planes,
    // is visually identical to rendering in XY 2D as long as this effect is
    // not rotated in space (which it is not). So we'll optimize by skipping
    // the Z axis for now.
    //
    // FUTURE: make a volumetric demo with legible Z axis, Cubatron-style.
    //
    // protected double devMinZ = 0.0;
    // protected double devDepth = 0.0;
    // protected double zCenterOffset = 0.0;
    // protected double zQuarterOffset = 0.0;

    public Class getStateClass() {
        return BloomEffectState.class;
    }

    public Object getState() {
        return new BloomEffectState(this.copyPalette(), enableBilateralSym,
                enableNestedBilateralSym);
    }

    public Pixel[] copyPalette() {
        Pixel[] p = null;
        if(palette != null) {
            p = new Pixel[palette.length];
            for(int i = 0; i < palette.length; i++) {
                p[i] = new Pixel(palette[i]);
            }
        }
        return p;
    }

    public void setState(Object state) throws ClassCastException {
        BloomEffectState command = (BloomEffectState) state;
        Pixel[] p = command.getPalette();

        // Deep copy the given palette, filling in missing items with black.
        // Ignore extra colors.
        if(p != null && p.length > 0) {
            int size = p.length;
            int max = BloomTiling.REPERTOIRES.length + 1;
            if(size > max) {
                size = max;
            }

            repertoire = BloomTiling.REPERTOIRES[size];
            tiling = BloomTiling.TILINGS_2D[repertoire.length];
            palette = new Pixel[size];

            for(int i=0; i<size; i++) {
                Pixel color = p[i];
                if(color == null) {
                    color = Pixel.black();
                } else {
                    color = new Pixel(color);
                }
                palette[i] = color;
            }
        }

        Boolean bilateral = command.getEnableBilateralSym();
        if(bilateral != null) {
            enableBilateralSym = bilateral;
        }

        Boolean nested = command.getEnableNestedBilateralSym();
        if(nested != null) {
            enableNestedBilateralSym = nested;
        }
    }

    public void patchDevices(Device[] devices) {
        super.patchDevices(devices);
        deviceBounds = Device.getDeviceBoundingCube(devices);
        devMinX = deviceBounds.getMinX();
        devMinY = deviceBounds.getMinY();
        devWidth = deviceBounds.getWidth();
        devHeight = deviceBounds.getHeight();
        xCenterOffset = devWidth * 0.5;
        xQuarterOffset = devWidth * 0.25;
        yCenterOffset = devHeight * 0.5;
        yQuarterOffset = devHeight * 0.25;

        // devMinZ = deviceBounds.getMinZ();
        // devDepth = deviceBounds.getDepth();
        // zCenterOffset = devDepth * 0.5;
        // zQuarterOffset = devDepth * 0.25;

    }


    public void animate(TimePoint timePoint) {
        Device dev = null;
        double[] xyz = null;
        double xoffset = 0.0;
        double yoffset = 0.0;

        // x and y palette index
        int px = 0;
        int py = 0;

        for (int i = 0; i < devices.length; i++) {
            dev = devices[i];
            xyz = dev.getPoint();

            // Symmetry is implemented as a transformation of each coordinate.
            if(enableX) {
                xoffset = xyz[0] - devMinX;
                if(enableBilateralSym) {
                    if(xoffset > xCenterOffset) {
                        xoffset = devWidth - xoffset;
                    }
                    if(enableNestedBilateralSym && xoffset > xQuarterOffset) {
                        xoffset = xCenterOffset - xoffset;
                    }
                }
                px = TriangularSequence.oscillatingTriangularRootColor(xoffset, scale, repertoire);
            } else {
                px = 0;
            }
            if(enableY) {
                yoffset = xyz[1] - devMinY;
                if(enableBilateralSym) {
                    if(yoffset > yCenterOffset) {
                        yoffset = devHeight - yoffset;
                    }
                    if(enableNestedBilateralSym && yoffset > yQuarterOffset) {
                        yoffset = yCenterOffset - yoffset;
                    }
                }
                py = TriangularSequence.oscillatingTriangularRootColor(yoffset, scale, repertoire);
            } else {
                py = 0;
            }

            int colorIndex = tiling[px][py];
            pixels[i].setColor(palette[colorIndex]);

            // TODO might need to crop out the SE vertical ascenders if I can't think of a good way to keep them from blinking...
            // alternative idea: only enable Y rather than X because there are not perfect horizontals
        }

        // Animate the scale.
        scale += scaleIncrement; // TODO - timebase, not framebase, this ++
        if(scale > 20.0) {
            scaleIncrement = -0.05; // keep increment small or it's too discontinuous to read the seizuretron
            scale = 20.0;
        } else if(scale < 1.0) {
            scaleIncrement = 0.05; // TODO - asymmetric increase and decrease? maybe faster increase?
            scale = 1.0;
        }
    }
}


class BloomTiling {

    /**
     * Objective:
     *    Create a tiling of n distinct colors that distributes those colors
     *    into a 2D square matrix of n * n elements (examples below).
     *
     * Rules:
     *    a) Two tiles with the same color must never be horizontally or
     *       vertically adjacent in a tiling.
     *
     *    b) Prefer not to cause diagonals, especially not strong
     *       diagonals, where two tiles with the same color are
     *       diagonally adjacent in a tiling.
     *
     *    c) Prefer approximately equal populations for each color.
     *
     * It is not possible to avoid diagonals completely, and it is
     * not possible to guarantee a perfectly equal distribution of
     * colors.
     *
     * Open questions (see below):
     *
     *    Q1) Are there some prime palette sizes for which there is no solution
     *        without violating rule a?
     *
     *    Q2) More generally, is there an efficient way to compute the number
     *        of 2D (raster-space) solutions for a given palette size?
     *
     *    Q3) How about 3D (voxel-space) tilings?
     *
     * Even numbers of colors can start by ringing the tile like this:
     *
     *    0 1 2 3 4 5
     *    1         4
     *    2         3
     *    3         2
     *    4         1
     *    5 4 3 2 1 0
     *
     * ...and then fill in the center tiles to taste, observing rules b and c.
     *
     * But odd numbers can't use this seed without placing their medians
     * horizontally and vertically adjacent:
     *
     *    0 1 2 3 4
     *    1   |   3
     *    2--BAD--2
     *    3   |   1
     *    4 3 2 1 0
     *
     * ...which is no good because the median 2s wrap around in violation of
     * rule a.
     *
     * Any non-prime number can be factored into its prime and then tiled
     * recursively using the tilings of its factors. For example, one 3
     * color tiling is this:
     *
     *    0 1 2
     *    1 2 0
     *    2 0 1
     *
     * Since 9 colors = 3 colors * 3 colors, a 9 color tiling is a 3-tiling
     * of 3 distinct palettes:
     *
     *       3 colors                   9 colors
     * =====================      =====================
     * 0 1 2 | 0 1 2 | 0 1 2      0 1 2 | 3 4 5 | 6 7 8
     * 1 2 0 | 1 2 0 | 1 2 0      1 2 0 | 4 5 3 | 7 8 6
     * 2 0 1 | 2 0 1 | 2 0 1      2 0 1 | 5 3 4 | 8 6 7
     * ------+-------+------      ------+-------+------
     * 0 1 2 | 0 1 2 | 0 1 2      3 4 5 | 6 7 8 | 0 1 2      palette 0: 0 1 2
     * 1 2 0 | 1 2 0 | 1 2 0  =>  4 5 3 | 7 8 6 | 1 2 0      palette 1: 3 4 5
     * 2 0 1 | 2 0 1 | 2 0 1      5 3 4 | 8 6 7 | 2 0 1      palette 2: 6 7 8
     * ------+-------+------      ------+-------+------
     * 0 1 2 | 0 1 2 | 0 1 2      6 7 8 | 0 1 2 | 3 4 5
     * 1 2 0 | 1 2 0 | 1 2 0      7 8 6 | 1 2 0 | 4 5 3
     * 2 0 1 | 2 0 1 | 2 0 1      8 6 7 | 2 0 1 | 5 3 4
     *
     * Given this fact, it would possible to generate a tiling for any
     * non-prime sized palette as long as it was possible to generate
     * a tiling for any prime number less than or equal to that palette
     * size. More specifically, we'd just ned to generating a tiling for
     * each prime factor of the given palette size. (See open questions Q1
     * and Q2.
     *
     * color index X/Y: as returned by oscillatingTriangularRootColor
     */
    public static final int[/*palette size*/][/*color index X*/][/*color index Y*/] TILINGS_2D = {

            // 0 colors
            {},

            // 1 color (solid)
            {
                    {0,},
            },

            // 2 colors (checkerboard)
            {
                    {0, 1,},
                    {1, 0,},
            },

            // 3 colors
            {
                    {0, 1, 2,},
                    {1, 2, 0,},
                    {2, 0, 1,},
            },

            // 4 colors (intentionally not tiled as 2 * 2 colors checkerboarded)
            {
                    {0, 1, 2, 3,},
                    {1, 3, 0, 2,},
                    {2, 0, 3, 1,},
                    {3, 2, 1, 0,},
            },

            // 5 colors
            {
                    {0, 2, 0, 2, 4,},
                    {1, 3, 4, 1, 3,},
                    {4, 0, 2, 0, 2,},
                    {3, 1, 4, 3, 1,},
                    {4, 3, 2, 1, 0,},
            },

            // 6 colors = 2 * 3 colors checkerboarded
            {
                    {0, 1, 2, /**/ 3, 4, 5,},
                    {1, 2, 0, /**/ 4, 5, 3,},
                    {2, 0, 1, /**/ 5, 3, 5,},

                    {3, 4, 5, /**/ 0, 1, 2,},
                    {4, 5, 3, /**/ 1, 2, 0,},
                    {5, 3, 5, /**/ 2, 0, 1,},
            },

            // 7 colors
            {
                    {0, 1, 2, 5, 4, 5, 6,},
                    {1, 3, 4, 0, 2, 3, 5,},
                    {2, 4, 6, 5, 6, 2, 4,},
                    {5, 0, 1, 0, 1, 6, 1,},
                    {4, 2, 6, 5, 0, 4, 2,},
                    {5, 3, 2, 6, 4, 3, 1,},
                    {6, 5, 4, 1, 2, 1, 0,},
            },

            // 8 colors = 2 * 4 colors checkerboarded
            {
                    {0, 1, 2, 3, /**/ 4, 5, 6, 7,},
                    {1, 3, 0, 2, /**/ 5, 7, 4, 6,},
                    {2, 0, 3, 1, /**/ 6, 4, 7, 5,},
                    {3, 2, 1, 0, /**/ 7, 6, 5, 4,},

                    {4, 5, 6, 7, /**/ 0, 1, 2, 3,},
                    {5, 7, 4, 6, /**/ 1, 3, 0, 2,},
                    {6, 4, 7, 5, /**/ 2, 0, 3, 1,},
                    {7, 6, 5, 4, /**/ 3, 2, 1, 0,},
            },

            // 9 colors = 3 * 3 colors (see docs above)
            {
                    {0, 1, 2, /**/ 3, 4, 5, /**/ 6, 7, 8,},
                    {1, 2, 0, /**/ 4, 5, 3, /**/ 7, 8, 6,},
                    {2, 0, 1, /**/ 5, 3, 4, /**/ 8, 6, 7,},

                    {3, 4, 5, /**/ 6, 7, 8, /**/ 0, 1, 2,},
                    {4, 5, 3, /**/ 7, 8, 6, /**/ 1, 2, 0,},
                    {5, 3, 4, /**/ 8, 6, 7, /**/ 2, 0, 1,},

                    {6, 7, 8, /**/ 0, 1, 2, /**/ 3, 4, 5,},
                    {7, 8, 6, /**/ 1, 2, 0, /**/ 4, 5, 3,},
                    {8, 6, 7, /**/ 2, 0, 1, /**/ 5, 3, 4,},
            },

    };

    /**
     * Statically defined trivial sequences for clarity of implementation.
     * FUTURE: dynamically generate these PALETTES if TILINGS_nD are ever
     * dynamically computed.
     */
    public static final int[/*palette size*/][/*color index*/] REPERTOIRES = {

            // 0 colors
            {},

            // 1 color (solid)
            { 0, },

            // 2 colors (checkerboard)
            { 0, 1, },

            // 3 colors
            { 0, 1, 2, },

            // 4 colors
            { 0, 1, 2, 3, },

            // 5 colors
            { 0, 1, 2, 3, 4, },

            // 6 colors
            { 0, 1, 2, 3, 4, 5, },

            // 7 colors
            { 0, 1, 2, 3, 4, 5, 6, },

            // 8 colors
            { 0, 1, 2, 3, 4, 5, 6, 7, },

            // 9 colors
            { 0, 1, 2, 3, 4, 5, 6, 7, 8,},
    };

}