package com.coillighting.udder;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.coillighting.udder.mix.Frame;

/** First stab at an Open Pixel Control network client.
 *  This class is responsible for translating the pixels in mixed down frames
 *  into OPC messages, then transmitting them to the OPC server.
 *
 *  Runs in its own thread, kicked off by ServicePipeline.
 *  Typically deployed as a singleton.
 *
 *  Note: If the OPC server says "OPC: Source -1 does not exist", check the
 *  very first error message. Its assigned port is probably already in use.
 *
 *  Note: WATCH OUT FOR JAVA'S EVIL SIGNED BYTES.
 */
public class OpcTransmitter implements Runnable {

	private BlockingQueue<Frame> frameQueue;
	private int maxDelayMillis;
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
		this.maxDelayMillis = 1500; // TODO: tune this

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
			byte[] message = null;
			while(true) {
				try {
					Frame frame = this.frameQueue.poll(this.maxDelayMillis,
						TimeUnit.MILLISECONDS);
					if(frame != null) {
						Pixel[] pixels = frame.getPixels();
						this.log("===============> pixels[0]=" + pixels[0]); // TEMP
						final int pixelLen = pixels.length;
						final int subpixelLen = 3 * pixelLen;
						final int headerLen = 4;
						final int messageLen = headerLen + subpixelLen;
						message = new byte[messageLen]; // TODO: recycle if len identical

						// boolean test = false;
						// if(test) {
						// 	// For now, just make a blinking, fading test pattern.

						// 	// Slowly fade out over many requests.
						// 	byte level = (byte) ((0xFF - frame.getValue()) % 256);

						// 	// Attempt to blink every 2nd frame for visibility.
						// 	if(level % 2 == 0) {
						// 		level = 0;
						// 	}
						// 	this.log("Received frame: " + frame
						// 		+ ". Generating test pattern, gray level " + ((int) level & 0xFF));
						// }

						// header: channel, 0 (??), length MSB, length LSB
						final byte channel = 0;

						// OPC protocol details (byte offsets)
						// TODO move these into an OpcHeader constants class
						final int CHANNEL = 0;
						final int COMMAND = 1;
						final int COMMAND_SET_PIXELS = 0;
						final int SUBPIXEL_COUNT_MSB = 2;
						final int SUBPIXEL_COUNT_LSB = 3;
						final int SUBPIXEL_START = 4;

						message[CHANNEL] = channel;
						message[COMMAND] = COMMAND_SET_PIXELS;

						message[SUBPIXEL_COUNT_MSB] = (byte)(subpixelLen / 256);
						message[SUBPIXEL_COUNT_LSB] = (byte)(subpixelLen % 256);

						// TODO consider relocating this into Frame
						int i=SUBPIXEL_START;
						for(Pixel pixel: pixels) {
							message[i] = (byte)(0xFF & (int)(255.99999f * pixel.r));
							message[i+1] = (byte) (0xFF & (int)(255.99999f * pixel.g));
							message[i+2] = (byte) (0xFF & (int)(255.99999f * pixel.b));
							i += 3;

							// message[i] = (byte)(0xFF & Float.floatToIntBits(255.99999f * pixel.r));
							// if(i == SUBPIXEL_START) { // TEMP
							// 	this.log("message[start] pixel.r:      " + pixel.r);
							// 	this.log("message[start] float:        " + 255.99999f * pixel.r);
							// 	int theint = (int)(255.99999f * pixel.r);
							// 	this.log("message[start] int:        " + theint);
							// 	this.log("message[start] &0xFF int:*  " + (0xFF & theint));
							// 	this.log("message[start] &0xFF (byte):*" + (byte)(0xFF & theint));

							// 	// this.log("message[start] intbits:      " + Float.floatToIntBits(255.99999f * pixel.r));
							// 	// this.log("message[start] &0xFF int:   " + (0xFF & Float.floatToIntBits(255.99999f * pixel.r)));
							// 	// this.log("message[start] &0xFF (byte): " + (byte)(0xFF & Float.floatToIntBits(255.99999f * pixel.r)));
							// }
							// message[i+1] = (byte) (0xFF & Float.floatToIntBits(255.99999f * pixel.g));
							// message[i+2] = (byte) (0xFF & Float.floatToIntBits(255.99999f * pixel.b));
							// i += 3;

						}
						this.sendBytes(message);
						this.log("Sent: " + this.formatMessage(message));
						// TODO recycle the frame datastructure?
					} else {
						// If there are no incoming frames, periodically retransmit
						// the last frame, in case the remote OPC server process was
						// restarted and needs its state refreshed.
						this.log("Received no new frame in the past "
							+ this.maxDelayMillis + " milliseconds. Retransmitting the "
							+ "previous frame (if any).");
						if(message != null) {
							this.sendBytes(message);
						}
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

	public String formatMessage(byte[] message) throws IOException {
		// Note: ((int) foo & 0xFF) causes a byte to be printed as if it were
		// an unsigned value. This is a regrettable Java idiom.
		if(message.length < 7) {
			throw new IOException("Each OPC message must be at least 7 bytes long.");
		}
		int i = 0;
		return "[ " + message.length + " bytes:"
			// OPC header
			+ " chan=" + ((int) message[i++] & 0xFF)
			+ " command=" + ((int) message[i++] & 0xFF)
			+ " lenmsb=" + ((int) message[i++] & 0xFF)
			+ " lenlsb=" + ((int) message[i++] & 0xFF)

			// Print the first 3 subpixels
			+ " message:"
			+ " R" + message[i] + "=" + (((int) message[i++]) & 0xFF)
			+ " G" + message[i] + "=" + (((int) message[i++]) & 0xFF)
			+ " B" + message[i] + "=" + (((int) message[i++]) & 0xFF)
			+ (message.length > 7 ? " ..." : "")
			+ " ]";
	}

	public void log(String msg) {
		System.err.println(msg);
	}
}
