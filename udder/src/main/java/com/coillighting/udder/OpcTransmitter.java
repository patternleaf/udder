package com.coillighting.udder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.coillighting.udder.Frame;


public class OpcTransmitter implements Runnable {

	private BlockingQueue<Frame> frameQueue;
	private int maxDelayMinutes;

	public OpcTransmitter(BlockingQueue<Frame> frameQueue) {
		if(frameQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies frames.");
		}
		this.frameQueue = frameQueue;
		this.maxDelayMinutes = 1;
	}

	public void run() {
		try {
			this.log("Starting OPC transmitter.");
			while(true) {
				Frame frame = this.frameQueue.poll(this.maxDelayMinutes,
					TimeUnit.MINUTES);
				if(frame!=null) {
					this.log("Received frame: " + frame + " (TODO: transmit)");
				} else {
					// If there are no incoming frames, periodically retransmit
					// the last frame, in case the remote OPC server process was
					// restarted and needs its state refreshed.
					this.log("Received no new frame in the past "
						+ this.maxDelayMinutes + " minutes. Retransmitting the "
						+ "previous frame (TODO).");
				}
			}
		} catch(InterruptedException e) {
			this.log("Stopping OPC transmitter.");
		}
	}

	public void log(String msg) {
		System.err.println(msg);
	}
}
