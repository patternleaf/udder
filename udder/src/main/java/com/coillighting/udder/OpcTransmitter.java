package com.coillighting.udder;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.coillighting.udder.Frame;


public class OpcTransmitter implements Runnable {

	private BlockingQueue<Frame> frameQueue;
	private int maxDelayMinutes;
	private Socket socket;
	private DataOutputStream dataOutputStream;
	private String serverAddress;
	private int serverPort;

	public OpcTransmitter(BlockingQueue<Frame> frameQueue) {
		if(frameQueue==null) {
			throw new NullPointerException(
				"ShowRunner requires a queue that supplies frames.");
		}
		this.frameQueue = frameQueue;
		this.maxDelayMinutes = 1;

		// TODO: pass in the user-configured properties as params
		this.serverAddress = "127.0.0.1";
		this.serverPort = 7890;
	}

	protected void connect() throws IOException {
		this.socket = null;
		this.dataOutputStream = null;

		this.log("Attempting to connect to OPC remote server at "
			+ this.serverAddress + ":"+ this.serverPort);

		this.socket = new Socket(this.serverAddress, this.serverPort);
		this.log("Connected.");
		OutputStream out = socket.getOutputStream();
		this.log("Got Socket OutputStream.");
		this.dataOutputStream = new DataOutputStream(out);
		this.log("Got DataOutputStream.");
	}

	protected void sendBytes(byte[] bytes) throws IOException {
		if(socket == null) {
			this.connect();
		}
		this.dataOutputStream.write(bytes, 0, bytes.length);
	}

	public void run() {
		try {
			this.log("Starting OPC transmitter.");
			while(true) {
				try {
					Frame frame = this.frameQueue.poll(this.maxDelayMinutes,
						TimeUnit.MINUTES);
					if(frame!=null) {
						byte level = (byte) frame.getValue();
						this.log("Received frame: " + frame
							+ ". Generating test pattern, gray level "
							+ level);

						int pixelLen = 1250;
						int subpixelLen = 3 * pixelLen;
						int headerLen = 4 + subpixelLen;
						int messageLen = headerLen + subpixelLen;
						byte[] message = new byte[messageLen];

						// header: channel, 0 (??), length MSB, length LSB
						byte channel = 0;

						// OPC protocol details (byte offsets)
						// TODO move these into an OpcHeader constants class
						int CHANNEL = 0;
						int UNKNOWN = 1; // TODO look this up, not sure what this byte represents
						int SUBPIXEL_COUNT_MSB = 2;
						int SUBPIXEL_COUNT_LSB = 3;
						int SUBPIXEL_START = 4;

						message[CHANNEL] = channel;
						message[UNKNOWN] = 0;

						// TODO verify the following conversion:
						message[SUBPIXEL_COUNT_MSB] = (byte)(subpixelLen / 256);
						message[SUBPIXEL_COUNT_LSB] = (byte)(subpixelLen % 256);

						for(int i=SUBPIXEL_START; i<messageLen; i++) {
							message[i] = level;
						}
						this.log("Sending message: " + message);
						this.sendBytes(message);
						this.log("Sent.");
						// TODO recycle the frame
					} else {
						// If there are no incoming frames, periodically retransmit
						// the last frame, in case the remote OPC server process was
						// restarted and needs its state refreshed.
						this.log("Received no new frame in the past "
							+ this.maxDelayMinutes + " minutes. Retransmitting the "
							+ "previous frame (TODO).");
					}
				} catch(IOException e) {
					this.log("\nERROR -----------------------------------------");
					this.log(e.toString());
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
