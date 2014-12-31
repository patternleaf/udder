package com.coillighting.udder.infrastructure;

public abstract class OpcTransmissionCouplingFactory {

    public static Transmitter create(SocketAddress serverAddr, int[] deviceAddressMap) {
        return new OpcTransmitter(serverAddr, null, deviceAddressMap.clone());
    }

}
