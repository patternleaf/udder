package com.coillighting.udder.infrastructure;

/** Boilerplate datastructure specifying where a server listens.
 * Less cranky than the JDK's InetSocketAddress.
 */
public class SocketAddress {

    protected String host;
    protected int port;

    public SocketAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}