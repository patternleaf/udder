package com.coillighting.udder.infrastructure;

import java.util.Queue;

import com.coillighting.udder.effect.Effect;
import com.coillighting.udder.mix.Frame;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

/** A ShowRunner owns all the infrastructure required to pump events through
 *  a Mixer which implements the current scenegraph. This object owns the scene
 *  itself, while its neighbors manage the infrastructure.
 *
 *  Runs in its own thread, started by ServicePipeline.
 *  Typically deployed as a singleton.
 */
public class ShowRunner implements Runnable {

    protected Queue<Command> commandQueue;
    protected Mixer mixer;
    protected Router router;
    protected Queue<Frame> frameQueue;

    protected boolean verbose = false;

    // Timing measurements.
    // Normally (busyWait=false) fps roughly equals 1000/frameDelayMillis.
    protected int frameDelayMillis = 10; // ignored if busywait
    protected boolean busyWait = false; // wait in a hot idle loop, not thread sleep
    protected long previousFrameRealTimeMillis = 0;
    protected long frameCounter = 0;

    public ShowRunner(Queue<Command> commandQueue, Mixer mixer,
        Router router, Queue<Frame> frameQueue)
    {
        if(commandQueue==null) {
            throw new NullPointerException(
                "ShowRunner requires a queue that supplies commands.");
        } else if(mixer==null) {
            throw new NullPointerException(
                "ShowRunner requires a Mixer that defines the scene.");
        } else if(router==null) {
            throw new NullPointerException(
                "ShowRunner requires a Router to send commands to scene elements.");
        } else if(frameQueue==null) {
            throw new NullPointerException(
                "ShowRunner requires a queue that supplies frames.");
        }
        this.commandQueue = commandQueue;
        this.mixer = mixer;
        this.router = router;
        this.frameQueue = frameQueue;
    }

    public void run() {
        try {
            // Immutable timepoint, passed down the chain to frames.
            TimePoint timePoint = new TimePoint();
            boolean sleepy=false;
            int droppedFrameCount = -1;
            final int droppedFrameLogInterval = 500;

            this.log("Starting show.");

            while(true) {

                Command command = this.commandQueue.poll();

                if(command != null || !sleepy) {
                    sleepy = true;
                    if(command != null) {
                        this.log(command); //TEMP
                        // this.log("           value: " + command.getValue()); //TEMP

                        // TODO - add routes for timer
                        String path = command.getPath();
                        Effect dest = this.router.get(path);
                        // TODO check for null dest
                        try {
                            dest.setState(command.getValue());
                        } catch(Exception e) {
                            this.log("Failed to issue command to destination "
                                + dest + " at " + path + ": " + e); // TEMP?
                        }
                    }
                    timePoint = timePoint.next();

                    if(verbose) {
                        long time = timePoint.realTimeMillis();
                        long latency = time - previousFrameRealTimeMillis;

                        // The JVM system time only comes in millis, but the nano
                        // timers are a can of worms (and AFAIK system-dependent),
                        // so we count frames until the clock changes in order to
                        // estimate framerate.
                        if(latency > 0) {
                            this.log("Command latency <= " + latency + " ms (" + frameCounter + " frames / " + latency + " ms) = " + (1000 * frameCounter/latency) + " fps");
                            previousFrameRealTimeMillis = time;
                            frameCounter = 1;
                        } else {
                            ++frameCounter;
                        }
                    }

                    this.mixer.animate(timePoint);

                    // FIXME clarify ownership of pixels.. render() isn't quite threadsafe yet.
                    Pixel[] pixels = this.mixer.render();
                    // FIXME REF: get this Frame straight from the Mixer?
                    Frame frame = new Frame(timePoint, pixels);

                    if(!frameQueue.offer(frame)) {
                        if(droppedFrameCount == -1) {
                            this.log("Frame queue overflow. Dropped frame " + timePoint);
                            droppedFrameCount = 1;
                        } else {
                            if(droppedFrameCount + 1 >= droppedFrameLogInterval) {
                                this.log("Frame queue overflow on frame "
                                    + timePoint + ". Dropped " + droppedFrameCount
                                    + " frames since the previous message like this.");
                                droppedFrameCount = 0;
                            } else {
                                ++droppedFrameCount;
                            }
                        }
                    }
                } else if(busyWait) {
                    // duration=10000 gave me 2000-5000 fps in a mix with
                    // more than 10 layers * 2 Kpixels, 1 of them animated.
                    // Performance degraded by roughly 20% when I animated 10 of
                    // them instead.
                    ShowRunner.waitBusy(10000);
                    sleepy = false;
                } else {
                    // Our crude timing mechanism currently does not account for
                    // the cost of processing each command.
                    Thread.sleep(this.frameDelayMillis);
                    sleepy = false;
                }
            }
        } catch(InterruptedException e) {
            this.log("Stopping show.");
        }
    }

    public void log(Object msg) {
        System.out.println(msg);
    }


    //--------------------------------------------------------------------------
    // TODO move timing utils

    private static volatile long waitSeed = System.nanoTime();

    /** Wait by forcing the CPU to spin for several cycles, correlated with
     *  the specified duration, evading optimizations that might rewrite this
     *  method into a no-op. An alternative to a quick thread sleep, which isn't
     *  very quick at these scales. EXPERIMENTAL.
     */
    public static void waitBusy(long duration) {
        // See research here:
        //    http://shipilev.net/blog/2014/nanotrusting-nanotime/
        // and benchmarking source here (formally GP2, but apparently derived
        // prior research into e.g. random-number generators etc.):
        //    (GPL2) http://hg.openjdk.java.net/code-tools/jmh/file/cde312963a3d/jmh-core/src/main/java/org/openjdk/jmh/logic/BlackHole.java#l400

        // Randomize, then reuse, the seed to avoid optimizations.
        long t = waitSeed;

        // See the article re: this backwards-counting trick.
        for (long i=duration; i>0; i--) {
            // 48 bit linear congruential generator with prime addend.
            t += (t * 0x5DEECE66DL + 0xBL + i) & (0xFFFFFFFFFFFFL);
        }

        // Memoization buster
        if (t == 42) {
            waitSeed += t;
        }
    }
}
