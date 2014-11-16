package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.infrastructure.Stateful;
import com.coillighting.udder.mix.StatefulAnimator;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** When your drawing routine can be patched, animated, and rendered, then
 *  it is ready to be loaded onto a Layer as an Effect.
 */
public interface Effect extends StatefulAnimator {

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

    /** Draw this object's current state as a pixel array. Normally, each
     *  Renderer is also an Animator, and each call to render() follows a call
     *  to animate(TimePoint). See Effect.
     */
    public Pixel[] render();
    // FIXME clarify ownership of the returned pixel array! it might be shared with
    // multiple downstream OPC transmitters. should probably copy it if there
    // are >1 downstream consumers. For now, it is crucial that the renturned
    // pixel array is owned soley by the caller, and that downstream consumers are
    // read-only, never write, on this array.

    // TODO convert all levels to double
    public void levelChanged(float oldLevel, float newLevel);
}
