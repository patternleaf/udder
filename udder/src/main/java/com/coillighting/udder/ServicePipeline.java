package com.coillighting.udder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Queue;
import org.boon.json.JsonFactory;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.Server;

import com.coillighting.udder.Command;
import com.coillighting.udder.HttpServiceContainer;
import com.coillighting.udder.OpcTransmitter;
import com.coillighting.udder.ShowRunner;


public class ServicePipeline {

	// Roughly in order of dependency:
	private boolean verbose = false;
	private Mixer mixer;
	private Queue<Command> commandQueue;
	private BlockingQueue<Frame> frameQueue;
	private HttpServiceContainer httpServiceContainer;
	private Server server;
	private int listenPort = 8080;
	private SocketAddress listenAddress;
	private Connection serverConnection;
	private ShowRunner showRunner;
	private Thread showThread;
	private OpcTransmitter opcTransmitter;
	private Thread transmitterThread;

	public ServicePipeline(Mixer mixer) throws IOException {
		if(mixer == null) {
			throw new NullPointerException(
				"ServicePipeline requires a Mixer that defines the scene.");
		}
		this.mixer = mixer;
        // TODO args or properties to set connection binding params,
        // framerate, runmode, log level, etc.
		this.verbose = true;
        this.commandQueue = new ConcurrentLinkedQueue<Command>();
        this.frameQueue = new LinkedBlockingQueue(32); // TODO shrink buffer size
        this.showRunner = new ShowRunner(this.commandQueue, this.mixer, this.frameQueue);
        this.showThread = new Thread(this.showRunner);
        this.opcTransmitter = new OpcTransmitter(this.frameQueue);
        this.transmitterThread = new Thread(this.opcTransmitter);

        // TODO rename to HttpServiceContainer or something
        this.httpServiceContainer = new HttpServiceContainer(this.commandQueue);
        this.httpServiceContainer.setVerbose(this.verbose);

        this.server = new ContainerServer((Container) this.httpServiceContainer);
		this.serverConnection = new SocketConnection(this.server);
		this.listenPort = 8080;
		this.listenAddress = new InetSocketAddress(this.listenPort);
    }

    public void start() throws IOException {
    	this.transmitterThread.start();
        this.showThread.start();
        this.serverConnection.connect(this.listenAddress);
        this.log("Listening on http://localhost:" + this.listenPort + '/');
        this.log("ListenAddress: " + this.listenAddress);
	}

	public void log(Object message) {
		System.err.println(message);
	}
}
