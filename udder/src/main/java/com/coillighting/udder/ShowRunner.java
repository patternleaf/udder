package com.coillighting.udder;

import java.util.Queue;

import com.coillighting.udder.Frame;
import com.coillighting.udder.Command;
import com.coillighting.udder.Mixer;


/** A ShowRunner owns all the infrastructure required to pump events through
 *  a Mixer which implements the current scenegraph. This object owns the scene
 *  itself, while its neighbors manage the infrastructure.
 *
 *  Runs in its own thread, started by ServicePipeline.
 *  Typically deployed as a singleton.
 */
public class ShowRunner implements Runnable {

	private Queue<Command> commandQueue;
	private Mixer mixer;
	private Queue<Frame> frameQueue;
	private int targetFrameRateMillis = 10;

	public ShowRunner(Queue<Command> commandQueue, Mixer mixer, Queue<Frame> frameQueue) {
		if(commandQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies commands.");
		} else if(mixer==null) {
			throw new NullPointerException(
				"ShowRunner requires a Mixer that defines the scene.");
		} else if(frameQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies frames.");
		}
		this.commandQueue=commandQueue;
		this.mixer=mixer;
		this.frameQueue = frameQueue;
	}

	public void run() {
		try {
			// Immutable, passed down the chain to frames.
			TimePoint timePoint = new TimePoint();
			this.log("Starting show.");
			while(true) {
				Command command = this.commandQueue.poll();
				if(command != null) {
					this.log("Received command: " + command);

					// (TEMP) TODO - don't hardcode this routing table. Handle out-of-bounds exception instead.
					Integer destination = command.getDestination();
					if(destination == 0) {
						Layer layer = (Layer) this.mixer.getLayer(0); // FIXME sort out interfaces so that getEffect is accessible without casting - maybe just a Mixer.getEffect()?
						Effect effect = layer.getEffect();
						try {
							effect.setState(command.getValue());
						} catch(ClassCastException e) {
							this.log("Failed setting state on destination "
								+ destination + ": " + e);
						}
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
