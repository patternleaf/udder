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
        final boolean enableY = false;

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

        // TODO parameterize scale modularization
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