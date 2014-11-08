package com.coillighting.udder;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import com.coillighting.udder.Device;

public class PatchSheet {

    // TODO: convert this to an array. No need for a list anymore.
    protected List<Device> modelSpaceDevices;

    /** Encode {opc_addr: device_index} as an array. Index (key) is OPC address,
     *  value is index in modelSpaceDevices, if any. A value of -1 means "not
     *  patched." Otherwise the value must be >= 0.
     */
    protected int[] deviceAddressMap;

    public PatchSheet(List<Device> modelSpaceDevices) throws IllegalArgumentException {
        this.modelSpaceDevices = modelSpaceDevices;

        // Establish the range and mapping of OPC addresses
        int maxAddr = Integer.MIN_VALUE;
        for(Device device: modelSpaceDevices) {
            int addr = device.getAddr();
            if(addr > maxAddr) {
                maxAddr = addr;
            }
        }

        deviceAddressMap = new int[maxAddr + 1];
        Arrays.fill(deviceAddressMap, -1);
        int i=0;
        for(Iterator<Device> it = modelSpaceDevices.iterator(); it.hasNext(); i++) {
            int addr = it.next().getAddr();
            if(deviceAddressMap[addr] != -1) {
                // TODO: custom exception
                throw new IllegalArgumentException("Address " + addr
                    + " appears more than once in the patch sheet.");
            }
            deviceAddressMap[addr] = i;
        }
    }

    /** Return a list of devices in the order originally specified by the user.
     *  By convention this should correspond somehow with how the devices are
     *  arranged in space.
     */
    public List<Device> getModelSpaceDevices() {
        return this.modelSpaceDevices;
    }

    /** Return a list of devices such that for any OPC address n, the device at
     *  devices[n] has the address n. If there is only null at devices[n],
     *  then no device was patched to that address.
     */
    public int[] getDeviceAddressMap() {
        return this.deviceAddressMap;
    }
}
