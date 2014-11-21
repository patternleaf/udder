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
     * "the patch" in lighting parlance).
     *
     * Although Devices are variable in the long-term, they are locally
     * constant in a typical sequence of frames. For this reason, and
     * considering that physical Devices are infrequently reconfigured, the
     * locally constant Device data (just its address and spatial position)
     * is set separately from pixels, which typically vary per frame.
     *
     * Expensive setup computations might be performed only when we
     * patchDevices, allowing us to animate pixels efficiently.
     */
    public void patchDevices(Device[] devices);

    /** Draw this object's current state as a pixel array. Normally, each
     * Renderer is also an Animator, and each call to render() follows a call
     * to animate(TimePoint). See Effect.
     *
     * In the interest of performance, Effects are permitted to return
     * either a new array or a direct reference to a persistent array
     * owned by the Effect, with the stipulation that the caller will not
     * modify the returned data, that the caller will not share it with
     * other threads, and that no references will be left laying around
     * when this render loop completes.
     *
     * When your whole tree of Mixers, Layers and Effects has rendered,
     * then use Frame.createByCopy(TimePoint, Pixel[]) to make your own
     * copy of the results. Don't share references to Pixels that belong
     * to Effects with others, especially not other threads.
     */
    public Pixel[] render();

    public void levelChanged(double oldLevel, double newLevel);
}
