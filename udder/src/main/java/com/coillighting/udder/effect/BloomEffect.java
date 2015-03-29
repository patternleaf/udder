package com.coillighting.udder.effect;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.TriangularSequence;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Device;

import static com.coillighting.udder.util.LogUtil.log;

/** TODO DOC */
public class BloomEffect extends EffectBase {

    protected BoundingCube deviceBounds = null;

    public Class getStateClass() {
        return BloomEffectState.class;
    }

    public Object getState() {
        return null; // TODO BloomEffectState
    }

    public void setState(Object state) throws ClassCastException {
        log("TODO: BlooomEffect.setState()");
    }

    public void patchDevices(Device[] devices) {
        super.patchDevices(devices);
        deviceBounds = Device.getDeviceBoundingCube(devices);

        // TEMP - run self test
        TriangularSequence.main(null);
    }

    protected double scale = 1.0;
    protected double scaleIncrement = 0.05;

    public void animate(TimePoint timePoint) {
        // FIXME TODO cache these on device change only
        final double devMinX = deviceBounds.getMinX();
        final double devMinY = deviceBounds.getMinY();
        final double devMinZ = deviceBounds.getMinZ();
        final double devMaxX = deviceBounds.getMaxX();
        final double devMaxY = deviceBounds.getMaxY();
        final double devMaxZ = deviceBounds.getMaxZ();
        final double devWidth = deviceBounds.getWidth();
        final double devHeight = deviceBounds.getHeight();
        final double devDepth = deviceBounds.getDepth();
        final double xCenterOffset = devWidth * 0.5;
        final double xQuarterOffset = devWidth * 0.25;
        final double yCenterOffset = devHeight * 0.5;
        final double yQuarterOffset = devHeight * 0.25;
        final double zCenterOffset = devDepth * 0.5;
        final double zQuarterOffset = devDepth * 0.25;

        final boolean enableBilateralSym = true;
        final boolean enableNestedBilateralSym = true; // only counts if bilateral is already enabled

        // easier to see in 1d, maybe stick with it?
        final boolean enableY = true;

        // enabling Z at the Dairy, which consists of two parallel planes,
        // is the same as operating in 2D as long as the effect is not
        // rotated in space (which it is not).
        // TODO only compute 3D breakpoints if enabled.
        // TODO compute all breakpoints, but only on device change? <<---
        final boolean enableZ = false;

        // scale: device space units per thread
//        double scale = 2.0; //TODO: figure out how coarse to make this, probably depends on how it interacts with various palettes
        int[] palette = {0, 1};
//        log("scale=" + scale);
        for (int i = 0; i < devices.length; i++) {
            Device dev = devices[i];
            double[] xyz = dev.getPoint();

            double xoffset = xyz[0] - devMinX;
            double yoffset = xyz[1] - devMinY;
            double zoffset = xyz[2] - devMinZ;

            if(enableBilateralSym) {
                if(xoffset > xCenterOffset) {
                    xoffset = devWidth - xoffset;
                }
                if(enableY && yoffset > yCenterOffset) {
                    // TODO only if 2+d
                    yoffset = devHeight - yoffset;
                }
                if(enableZ && zoffset > zCenterOffset) {
                    // TODO only if 2+d
                    zoffset = devDepth - zoffset;
                }
                if(enableNestedBilateralSym) {
                    if(xoffset > xQuarterOffset) {
                        xoffset = xCenterOffset - xoffset;
                    }
                    if(enableY && yoffset > yQuarterOffset) {
                        yoffset = yCenterOffset - yoffset;
                    }
                    if(enableZ && zoffset > zQuarterOffset) {
                        zoffset = zCenterOffset - zoffset;
                    }
                }
            }
            // TODO 3d!

            int px = TriangularSequence.oscillatingTriangularRootColor(xoffset, scale, palette);
//            log("p=" + p);
            int py = TriangularSequence.oscillatingTriangularRootColor(yoffset, scale, palette);
            int pz = TriangularSequence.oscillatingTriangularRootColor(zoffset, scale, palette);


            // TODO palette mapping
            // TODO what to do with more than 2 items in palette - determine algo for checkerboarding this

            // TODO REF to dimensionality rather than combinatorial X and/or Y and/or Z logic
            if(enableZ && !enableY) {
                // XZ
                if (px != pz) {
                    pixels[i].setWhite();
                } else {
                    pixels[i].setBlack();
                }
            } else if(!enableZ && enableY) {
                // XY
                if (px != py) {
                    pixels[i].setWhite();
                } else {
                    pixels[i].setBlack();
                }
            } else if(enableZ && enableY) {
                // XYZ TODO
            } else {
                // X only
                if(px == 0) {
                    pixels[i].setWhite();
                } else {
                    pixels[i].setBlack();
                }
            }

            // might need to crop out the SE vertical ascenders if I can't think of a good way to keep them from blinking...
            // alternative idea: only enable Y rather than X because there are not perfect horizontals
        }

        // TODO parameterize scale modulation?
        scale += scaleIncrement; // TODO - timebase, not framebase, this ++
        if(scale > 20.0) {
            scaleIncrement = -0.05; // keep increment small or it's too discontinuous to read the seizuretron
            scale = 20.0;
        } else if(scale < 1.0) {
            scaleIncrement = 0.05; // TODO - asymmetric increase and decrease?
            scale = 1.0;
        }
    }
}

class BloomTiling {

    // Objective:
    //    Create a tiling of n distinct colors that distributes those colors
    //    into a square matrix of n * n elements (examples below).
    //
    // Rules:
    //    a) Two tiles with the same color must never be horizontally or
    //       vertically adjacent in a tiling.
    //
    //    b) Prefer not to cause diagonals, especially not strong
    //       diagonals, where two tiles with the same color are
    //       diagonally adjacent in a tiling.
    //
    //    c) Prefer approximately equal populations for each color.
    //
    // It is not possible to avoid diagonals completely, and it is
    // not possible to guarantee a perfectly equal distribution of
    // colors.
    //
    // Open questions (see below):
    //
    //    Q1) Are there some prime palette sizes for which there is no solution
    //        without violating rule a?
    //
    //    Q2) More generally, is there an efficient way to compute the number
    //        of solutions for a given palette size?
    //
    // Even numbers of colors can start by ringing the tile like this:
    //
    //    0 1 2 3 4 5
    //    1         4
    //    2         3
    //    3         2
    //    4         1
    //    5 4 3 2 1 0
    //
    // ...and then fill in the center tiles to taste, observing rules b and c.
    //
    // But odd numbers can't use this seed without placing their medians
    // horizontally and vertically adjacent:
    //
    //    0 1 2 3 4
    //    1   |   3
    //    2--BAD--2
    //    3   |   1
    //    4 3 2 1 0
    //
    // ...which is no good because the median 2s wrap around in violation of
    // rule a.
    //
    // Any non-prime number can be factored into its prime and then tiled
    // recursively using the tilings of its factors. For example, one 3
    // color tiling is this:
    //
    //    0 1 2
    //    1 2 0
    //    2 0 1
    //
    // Since 9 colors = 3 colors * 3 colors, a 9 color tiling is a 3-tiling
    // of 3 distinct palettes:
    //
    //       3 colors                   9 colors
    // =====================      =====================
    // 0 1 2 | 0 1 2 | 0 1 2      0 1 2 | 3 4 5 | 6 7 8
    // 1 2 0 | 1 2 0 | 1 2 0      1 2 0 | 4 5 3 | 7 8 6
    // 2 0 1 | 2 0 1 | 2 0 1      2 0 1 | 5 3 4 | 8 6 7
    // ------+-------+------      ------+-------+------
    // 0 1 2 | 0 1 2 | 0 1 2      3 4 5 | 6 7 8 | 0 1 2      palette 0: 0 1 2
    // 1 2 0 | 1 2 0 | 1 2 0  =>  4 5 3 | 7 8 6 | 1 2 0      palette 1: 3 4 5
    // 2 0 1 | 2 0 1 | 2 0 1      5 3 4 | 8 6 7 | 2 0 1      palette 2: 6 7 8
    // ------+-------+------      ------+-------+------
    // 0 1 2 | 0 1 2 | 0 1 2      6 7 8 | 0 1 2 | 3 4 5
    // 1 2 0 | 1 2 0 | 1 2 0      7 8 6 | 1 2 0 | 4 5 3
    // 2 0 1 | 2 0 1 | 2 0 1      8 6 7 | 2 0 1 | 5 3 4
    //
    // Given this fact, it would possible to generate a tiling for any
    // non-prime sized palette as long as it was possible to generate
    // a tiling for any prime number less than or equal to that palette
    // size. More specifically, we'd just ned to generating a tiling for
    // each prime factor of the given palette size. (See open questions Q1
    // and Q2.

    public static final int[/*dimensionality*/][/*palette size*/][/*color index*/] TILINGS = {

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
}