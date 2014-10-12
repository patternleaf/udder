package com.coillighting.udder;

import java.util.List;

import com.coillighting.udder.Device;


/** Something which can receive a list of Devices. */
public interface Patchable extends Animator {

    /** Called occasionally when somebody changes the device list (a.k.a.
     *  "the patch" in lighting parlance).
     *
     *  Although Devices are variable in the long-term, they are locally
     *  constant in a typical sequence of frames. For this reason, and
     *  considering that physical Devices are infrequently reconfigured, the
     *  locally constant Device data (just its address and spatial position)
     *  is set separately from pixels, which typically vary per frame.
     *
     *  Expensive setup computations might be performed only when we
     *  patchDevices, allowing us to animate pixels efficiently.
     */
    public void patchDevices(List<Device> devices);

}
