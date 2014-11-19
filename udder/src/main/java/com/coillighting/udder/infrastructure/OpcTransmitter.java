package com.coillighting.udder.infrastructure;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.coillighting.udder.mix.Frame;
import com.coillighting.udder.mix.TimePoint;
import com.coillighting.udder.model.Pixel;

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

    protected BlockingQueue<Frame> frameQueue;
    protected int maxDelayMillis;
    protected Socket socket;
    protected DataOutputStream dataOutputStream;
    protected String serverHost;
    protected int serverPort;
    protected int[] deviceAddressMap; // see PatchSheet.deviceAddressMap
    protected long previousFrameRealTimeMillis = 0;

    // TODO reorganize logging levels
    protected final boolean verbose = false;
    protected final boolean debug = true;

    public OpcTransmitter(SocketAddress opcServerAddr,
                          BlockingQueue<Frame> frameQueue,
                          int[] deviceAddressMap)
    {
        if(frameQueue==null) {
            throw new NullPointerException(
                "ShowRunner requires a queue that supplies frames.");
        }
        this.frameQueue = frameQueue;
        this.maxDelayMillis = 15000; // FUTURE: allow the user to tune this

        this.serverHost = opcServerAddr.getHost();
        this.serverPort = opcServerAddr.getPort();
        this.deviceAddressMap = deviceAddressMap;
    }

    protected void connect() throws IOException {
        this.socket = null;
        this.dataOutputStream = null;

        this.log("Attempting to connect to OPC remote server at "
            + this.serverHost + ":"+ this.serverPort);

        this.socket = new Socket(this.serverHost, this.serverPort);
        this.log("Connected " + this);
        OutputStream out = socket.getOutputStream();
        this.dataOutputStream = new DataOutputStream(out);
        this.log("Got socket DataOutputStream to " + this);
    }

    protected void sendBytes(byte[] bytes) throws IOException {
        if(socket == null) {
            this.connect();
        }
        this.dataOutputStream.write(bytes, 0, bytes.length);
        if(verbose) {
            this.log(this.formatMessage(bytes));
        }
    }

    public void run() {
        try {
            this.log("Starting OPC transmitter " + this);
            final Pixel black = new Pixel(0.0f, 0.0f, 0.0f);
            byte[] message = new byte[0];
            while(true) {
                try {
                    Frame frame = this.frameQueue.poll(this.maxDelayMillis,
                        TimeUnit.MILLISECONDS);

                    if(frame != null) {

                        if(verbose && debug) {
                            // Roughly clock frame timing.
                            long time = frame.getTimePoint().realTimeMillis();
                            long latency = time - previousFrameRealTimeMillis;
                            this.log("OPC frame latency: " + latency + " ms");
                            previousFrameRealTimeMillis = time;
                        }

                        // Pixels per Device, listed in device order, i.e. in
                        // the order of PatchSheet.modelSpaceDevices.
                        Pixel[] pixels = frame.getPixels();

                        // count the opc pixels, which might be a superset of
                        // the patched pixels:
                        final int pixelLen = deviceAddressMap.length;
                        final int subpixelLen = 3 * pixelLen;
                        final int headerLen = 4;
                        final int messageLen = headerLen + subpixelLen;

                        // recycle the message struct whenever possible
                        if(messageLen != message.length) {
                            message = new byte[messageLen];
                        }

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
                        for(int deviceIndex: deviceAddressMap) {
                            Pixel pixel;
                            if(deviceIndex < 0 || deviceIndex >= pixels.length) {
                                pixel = black;
                            } else {
                                pixel = pixels[deviceIndex];
                            }
                            message[i] = (byte)(0xFF & (int)(255.99999f * pixel.r));
                            message[i+1] = (byte) (0xFF & (int)(255.99999f * pixel.g));
                            message[i+2] = (byte) (0xFF & (int)(255.99999f * pixel.b));
                            i += 3;
                        }
                        this.sendBytes(message);
                        // TODO recycle the frame datastructure
                    } else {
                        // If there are no incoming frames, periodically retransmit
                        // the last frame, in case the remote OPC server process was
                        // restarted and needs its state refreshed.
                        if(message != null) {
                            this.log("Received no new frame in the past "
                                + this.maxDelayMillis
                                + " milliseconds. Retransmitting the previous frame in "
                                + this + '.');
                            this.sendBytes(message);
                        } else {
                            this.log("Received no new frame in the past "
                                + this.maxDelayMillis
                                + " milliseconds. Awaiting the first frame in "
                                + this + '.');
                        }
                    }
                } catch(SocketException e) {
                    this.log("\nERROR -----------------------------------------");
                    this.socket = null;
                    this.dataOutputStream = null;
                    this.log(e.toString());
                    this.delayReconnect();
                } catch(IOException e) {
                    this.log("\nERROR -----------------------------------------");
                    this.log(e.toString());
                }
            }
        } catch(InterruptedException e) {
            this.log("Stopping OPC transmitter " + this);
        }
    }

    // We break this out into a separate method so that a profiler can easily
    // distinguish between a real hotspot and a quick nap.
    protected void delayReconnect() throws InterruptedException {
        int timeout = 10000;
        this.log("Waiting " + timeout + " milliseconds before attempting reconnection to OPC server...");
        Thread.currentThread().sleep(timeout);
    }

    public String formatMessage(byte[] message) throws IOException {

        // Note: ((int) foo & 0xFF) causes a byte to be printed as if it were
        // an unsigned value. This is a regrettable Java idiom.
        if(message.length < 7) {
            throw new IOException("Each OPC message must be at least 7 bytes long.");
        }

        int i = 0;
        StringBuffer log = new StringBuffer("[ " + message.length + " bytes:"
            // OPC header
            + " chan=" + (0xFF & (int) message[i++])
            + " command=" + (0xFF & (int) message[i++])
            + " lenmsb=" + (0xFF & (int) message[i++])
            + " lenlsb=" + (0xFF & (int) message[i++])

            // Print the first 3 subpixels
            + " message:"
            + " R" + message[i] + "=" + (0xFF & (int) message[i++])
            + " G" + message[i] + "=" + (0xFF & (int) message[i++])
            + " B" + message[i] + "=" + (0xFF & (int) message[i++])
            + (message.length > 7 ? " ..." : "")
            + " ]");

        if(debug) {
            log.append('\n');

            // csv header
            log.append("buffer_index,subpixel_offset,component,value\n");

            for(int j=0; j<message.length; j++) {
                int subpixelOffset = j - 4;
                String component = "";
                if(subpixelOffset >= 0) {
                    int componentIdx = subpixelOffset % 3;
                    if(componentIdx == 0) {
                        component = "R";
                    } else if(componentIdx == 1) {
                        component = "G";
                    } else  {
                        component = "B";
                    }
                }

                log.append("" + j + ',' + subpixelOffset + ',' + component + ','
                    + (0xFF & (int) message[j]) + '\n');

                // To break off the output after you reach a certain device:
                // if(subpixelOffset > 3 * 60) break;
            }
            log.append('\n');
        }
        return log.toString();
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    public String toString() {
        return "OpcTransmitter(" + serverHost + ":" + serverPort + ")";
    }
}
