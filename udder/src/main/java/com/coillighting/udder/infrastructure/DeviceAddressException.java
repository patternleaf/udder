package com.coillighting.udder.infrastructure;

/** The program encountered a missing or invalid address for a Device. */
public class DeviceAddressException extends Exception {

    public DeviceAddressException(String message) {
        super(message);
    }

}