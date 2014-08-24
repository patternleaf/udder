package com.coillighting.udder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import org.boon.json.JsonFactory;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.Server;

import com.coillighting.udder.Command;
import com.coillighting.udder.HttpServiceContainer;
import com.coillighting.udder.ShowRunner;


public class ServicePipeline {

	// Roughly in order of dependency:
	private boolean verbose = false;
	private Queue<Command> commandQueue;
	private HttpServiceContainer httpServiceContainer;
	private Server server;
	private int listenPort = 8080;
	private SocketAddress listenAddress;
	private Connection serverConnection;
	private Thread showThread;
	private ShowRunner showRunner;

	public ServicePipeline() throws IOException {
        // TODO args or properties to set connection binding params,
        // framerate, runmode, log level, etc.
		this.verbose = true;
        this.commandQueue = new ConcurrentLinkedQueue<Command>();
        this.showRunner = new ShowRunner(this.commandQueue);
        this.showThread = new Thread(this.showRunner);

        // TODO rename to HttpServiceContainer or something
        this.httpServiceContainer = new HttpServiceContainer(this.commandQueue);
        this.httpServiceContainer.setVerbose(this.verbose);

        this.server = new ContainerServer((Container) this.httpServiceContainer);
		this.serverConnection = new SocketConnection(this.server);
		this.listenPort = 8080;
		this.listenAddress = new InetSocketAddress(this.listenPort);
    }

    public void start() throws IOException {
        this.showThread.start();
        this.serverConnection.connect(this.listenAddress);
        this.log("Listening on http://localhost:" + this.listenPort + '/');
        this.log("ListenAddress: " + this.listenAddress);
	}

	public void log(Object message) {
		System.err.println(message);
	}
}
