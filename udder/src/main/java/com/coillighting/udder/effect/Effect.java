package com.coillighting.udder.effect;

import java.util.List;

import com.coillighting.udder.mix.Animator;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.model.Pixel;

/** When your drawing routine can be patched, animated, and rendered, then
 *  it is ready to be loaded onto a Layer as an Effect.
 */
public interface Effect extends Animator {

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
    // FIXME clarify ownership of the returned pixel array

    /** Return the Class whose instances communicate the public parameters
     *  constituting this object's visible state.
     */
    public Class getStateClass();

    /** Return an instance of the state Class specifying the values of the
     *  public parameters in this object's visible state.
     */
    public Object getState();

    /** Where Object state is an instance of the state Class, set the values of
     *  the public parameters in this object's visible state, and adjust any
     *  private state variables for consistency with the specified values.
     */
    public void setState(Object state) throws ClassCastException;

}
