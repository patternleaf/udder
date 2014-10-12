package com.coillighting.udder;

import java.util.Queue;

import com.coillighting.udder.mix.Frame;
import com.coillighting.udder.mix.Mixer;

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
    protected int targetFrameRateMillis = 10;

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
            this.log("Starting show.");
            while(true) {
                Command command = this.commandQueue.poll();
                if(command != null) {
                    this.log("Received command: " + command);

                    // TODO - add routes for timer
                    String path = command.getPath();
                    Effect dest = this.router.get(path);
                    // TODO check for null dest
                    try {
                        dest.setState(command.getValue());
                    } catch(Exception e) {
                        this.log("Failed to issue command to destination "
                            + dest + " at " + path + ": " + e); // TEMP
                    }

                    timePoint = timePoint.next();
                    this.mixer.animate(timePoint);

                    // FIXME clarify ownership of pixels.. render() isn't quite threadsafe yet.
                    Pixel[] pixels = this.mixer.render();
                    // FIXME REF: get this Frame straight from the Mixer?
                    Frame frame = new Frame(timePoint, pixels);

                    if(!frameQueue.offer(frame)) {
                        this.log("Frame queue overflow. Dropped frame " + timePoint);
                    }
                } else {
                    // Our crude timing mechanism currently does not account for
                    // the cost of processing each command.
                    Thread.sleep(this.targetFrameRateMillis);
                }
            }
        } catch(InterruptedException e) {
            this.log("Stopping show.");
        }
    }

    public void log(String msg) {
        System.err.println(msg);
    }
}
