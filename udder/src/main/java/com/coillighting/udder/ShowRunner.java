package com.coillighting.udder;

import java.util.Queue;

import com.coillighting.udder.Frame;
import com.coillighting.udder.Command;


public class ShowRunner implements Runnable {

	private Queue<Command> commandQueue;
	private Queue<Frame> frameQueue;
	private int targetFrameRateMillis = 10;

	public ShowRunner(Queue<Command> commandQueue, Queue<Frame> frameQueue) {
		if(commandQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies commands.");
		} else if(frameQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies frames.");
		}
		this.commandQueue=commandQueue;
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
					this.log("Received command: " + command + " (TODO: render a frame)");
					timePoint = timePoint.next();
					Frame frame = new Frame(timePoint, command.getValue());
					if(!frameQueue.offer(frame)) {
						this.log("Frame queue overflow. Dropped frame "
							+ timePoint.getFrameIndex() + ".");
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
