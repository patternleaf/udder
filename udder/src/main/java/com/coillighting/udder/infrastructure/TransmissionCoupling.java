package com.coillighting.udder.infrastructure;

import com.coillighting.udder.infrastructure.Transmitter;
import com.coillighting.udder.mix.Frame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Supply a Transmitter with Frames via a threadsafe queue that blocks on
 * output until at least one frame is available. The transmitter runs in its
 * own thread.
 */
public class TransmissionCoupling {

    /** The smaller the buffer, the easier it will be to overload it by
     * dumping too many back-to-back commands into Udder. However, it
     * doesn't make a lot of sense to backlog frames, since this just
     * adds undesirable latency between the rendering of a frame and
     * its transmission to the OPC server.
     *
     * TODO: Don't re-render a whole new frame with each command
     * (see notes in ShowRunner). Densely spaced commands should all apply
     * to the upcoming frame.
     *
     * FUTURE: Consider whether to check for a backlog (and skip a frame)
     * before rendering a new one. Currently we drop a frame at the last
     * possible moment, after rendering it, because on a multicore machine,
     * rendering doesn't block consumption and transmission of output frames
     * from the frame queue(s). However, on a single core Beaglebone Black
     * with too many layers, and on a Raspberry Pi with even simple shows, we
     * have observed (11/14 - 12/04/14) quasiperiodic playback dragging
     * characterized by approx 0.5 sec moments of "sticky" playback. This
     * dragging may be due to CPU contention between threads. (Search for
     * "stick" for related notes and clues elsewhere.)
     *
     * We could possibly afford reduce bufferSize to 1, but we'd want to
     * check for excessive frame drops in the logs on the Beaglebone server
     * before doing so.
     */
    public static final int bufferSize = 2;

    protected BlockingQueue<Frame> frameQueue = null;
    protected Transmitter transmitter = null;
    protected Thread transmitterThread = null;

    public TransmissionCoupling(Transmitter transmitter) {
        frameQueue = new LinkedBlockingQueue<Frame>(bufferSize);
        transmitter.setFrameQueue(frameQueue);
        transmitterThread = new Thread(transmitter);
    }

    public void start() throws IllegalThreadStateException {
        this.transmitterThread.start();
    }

    public BlockingQueue<Frame> getFrameQueue() {
        return frameQueue;
    }

    public void setFrameQueue(BlockingQueue<Frame> frameQueue) {
        this.frameQueue = frameQueue;
    }

    public Transmitter getTransmitter() {
        return transmitter;
    }

    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    public Thread getTransmitterThread() {
        return transmitterThread;
    }

    public void setTransmitterThread(Thread transmitterThread) {
        this.transmitterThread = transmitterThread;
    }

}
