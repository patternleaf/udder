package com.coillighting.udder.infrastructure;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.Server;

import com.coillighting.udder.mix.Frame;
import com.coillighting.udder.mix.Mixer;

import static com.coillighting.udder.util.LogUtil.log;

/** Udder's central patchbay. Owns references to all of the persistent entities
 *  in the program. Manages Thread lifecycles for each components.
 *
 *  Normally runs in the main thread, started by Main.
 *  Typically deployed as a singleton.
 */
public class ServicePipeline {

    protected boolean verbose = false;
    protected Mixer mixer;
    protected Router router;
    protected Queue<Command> commandQueue;
    protected HttpServiceContainer httpServiceContainer;
    protected Server server;
    protected int listenPort = 8080;
    protected InetSocketAddress listenAddress;
    protected Connection serverConnection;
    protected List<TransmissionCoupling> transmissionCouplings;
    protected ShowRunner showRunner;
    protected Thread showThread;

    public ServicePipeline(Mixer mixer,
                           int[] deviceAddressMap,
                           Integer frameDelayMillis,
                           SocketAddress udderAddr,
                           List<SocketAddress> opcServerAddresses) throws IOException
    {
        if(mixer == null) {
            throw new NullPointerException(
                "ServicePipeline requires a Mixer that defines the scene.");
        }
        this.mixer = mixer;

        List<Queue<Frame>> frameQueues = null;
        int outputCt = 0;

        if(opcServerAddresses == null) {
            throw new NullPointerException(
                    "ServicePipeline requires a list of one or more OPC server addresses.");
        } else {
            outputCt = opcServerAddresses.size();
            if(outputCt == 0) {
                throw new IllegalArgumentException(
                        "ServicePipeline received an empty list of OPC server addresses. At least one is required.");
            } else {
                transmissionCouplings = new ArrayList<TransmissionCoupling>(outputCt);
                frameQueues = new ArrayList<Queue<Frame>>(outputCt);
            }
        }

        this.router = new Router();

        // TODO variable base path token for mixer - add constructor arg so we can have multiple mixers
        this.router.addRoutes("mixer0", this.mixer);

        this.commandQueue = new ConcurrentLinkedQueue<Command>();

        for(int i=0; i<outputCt; i++) {
            TransmissionCoupling coupling = new TransmissionCoupling(opcServerAddresses.get(i), deviceAddressMap);
            transmissionCouplings.add(coupling);
            frameQueues.add(coupling.frameQueue);
        }
        this.showRunner = new ShowRunner(
                frameDelayMillis,
                this.commandQueue,
                this.mixer,
                this.router,
                frameQueues);
        this.showThread = new Thread(this.showRunner);

        this.httpServiceContainer = new HttpServiceContainer(
            this.commandQueue,
            this.router.getCommandMap());
        this.httpServiceContainer.setVerbose(this.verbose || this.httpServiceContainer.getVerbose());

        this.server = new ContainerServer(this.httpServiceContainer);
        this.serverConnection = new SocketConnection(this.server);
        this.listenPort = udderAddr.getPort();
        this.listenAddress = new InetSocketAddress(udderAddr.getHost(), this.listenPort);
    }

    public void start() throws IOException {
        try {
            for(TransmissionCoupling coupling: transmissionCouplings) {
                log("Will transmit OPC frames to " + coupling.getOpcTransmitter());
                coupling.start();
            }
            this.showThread.start();
            this.serverConnection.connect(this.listenAddress);
            log("Listening on http://localhost:" + this.listenPort + '/');
            log("ListenAddress: " + this.listenAddress);
        } catch(BindException be) {
            log(be);
            log("Another process is already listening on " + this.listenAddress + ".");
            System.exit(1);
        }
    }
}

/** Supply an OpcTransmitter with Frames via a threadsafe queue that blocks on
 * output until at least one frame is available. The transmitter runs in its
 * own thread.
 */
class TransmissionCoupling {

    /** The smaller the buffer, the easier it will be to overload it by
     * dumping too many back-to-back commands into Udder. However, it
     * doesn't make a lot of sense to backlog frames, since this just
     * adds undesirable latency between the rendering of a frame and
     * its transmission to the OPC server.
     *
     * FUTURE: Consider whether to check for a backlog (and skip a frame)
     * before rendering a new one. Currently we drop a frame at the last
     * possible moment, after rendering it, because on a multicore machine,
     * rendering doesn't block consumption and transmission of output frames
     * from the frame queue(s). However, on a single core Beaglebone Black
     * with too many layers, and on a Raspberry Pi with even simple shows, we
     * have observed (11/14 - 12/04/14) quasiperiodic playback dragging
     * characterized by approx 0.5 sec moments of "sticky" playback. This
     * dragging may be due to CPU contention between threads. (Search for
     * "stick" for related notes and clues elsewhere.)
     *
     * We could probably afford reduce bufferSize to 1, but we'd want to
     * check for excessive frame drops in the logs on the Beaglebone server
     * before doing so.
     */
    public static final int bufferSize = 2;

    protected BlockingQueue<Frame> frameQueue = null;
    protected OpcTransmitter opcTransmitter = null;
    protected Thread transmitterThread = null;

    public TransmissionCoupling(SocketAddress serverAddr, int[] deviceAddressMap) {
        frameQueue = new LinkedBlockingQueue<Frame>(bufferSize);
        opcTransmitter = new OpcTransmitter(serverAddr, frameQueue,
            deviceAddressMap.clone());
        transmitterThread = new Thread(opcTransmitter);
    }

    public void start() throws IllegalThreadStateException {
        this.transmitterThread.start();
    }

    public BlockingQueue<Frame> getFrameQueue() {
        return frameQueue;
    }

    public void setFrameQueue(BlockingQueue<Frame> frameQueue) {
        this.frameQueue = frameQueue;
    }

    public OpcTransmitter getOpcTransmitter() {
        return opcTransmitter;
    }

    public void setOpcTransmitter(OpcTransmitter opcTransmitter) {
        this.opcTransmitter = opcTransmitter;
    }

    public Thread getTransmitterThread() {
        return transmitterThread;
    }

    public void setTransmitterThread(Thread transmitterThread) {
        this.transmitterThread = transmitterThread;
    }
}