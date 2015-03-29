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

    public void animate(TimePoint timePoint) {
        final double devMinX = deviceBounds.getMinX();
        final double devMinY = deviceBounds.getMinY();
        final double devWidth = deviceBounds.getWidth();
        final double devHeight = deviceBounds.getHeight();

        int[] palette = {0, 1};

        for (int i = 0; i < devices.length; i++) {
            Device dev = devices[i];
            double[] xyz = dev.getPoint();
            double scale = 2.0;
            log("width= " + devWidth + " offset=" + (xyz[0] - devMinX) + " scale=" + scale);
            int p = TriangularSequence.oscillatingTriangularRootColor(
                    xyz[0] - devMinX, scale, palette);
            log("p=" + p);
            if(p == 0) {
                pixels[i].setWhite();
            } else {
                pixels[i].setBlack();
            }
        }
    }
}