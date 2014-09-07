package com.coillighting.udder;

import java.util.Queue;

import com.coillighting.udder.Frame;
import com.coillighting.udder.Command;
import com.coillighting.udder.Mixer;


/** A ShowRunner owns all the infrastructure required to pump events through
 *  a Mixer which implements the current scenegraph.
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
				if(command!=null) {
					this.log("Received command: " + command);
					timePoint = timePoint.next();
					this.mixer.animate(timePoint);
					// TODO frame = mixer.mixWith(background) or whatnot
					Frame frame = new Frame(timePoint, command.getValue()); // TEMP
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
