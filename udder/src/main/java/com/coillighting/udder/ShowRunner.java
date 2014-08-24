package com.coillighting.udder;

import java.util.Queue;

import com.coillighting.udder.Command;


public class ShowRunner implements Runnable {

	private Queue<Command> queue;
	private int targetFrameRateMillis = 10;

	public ShowRunner(Queue<Command> queue) {
		if(queue==null) {
			throw new NullPointerException(
				"ShowRunner requires a Queue for supplying commands.");
		}
		this.queue=queue;
	}

	public void run() {
		try {
			this.log("Starting show.");
			while(true) {
				Command command = this.queue.poll();
				if(command!=null) {
					this.log("Received command: " + command);
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
