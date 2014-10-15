package com.coillighting.udder;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import com.coillighting.udder.Device;

public class PatchSheet {

    // TODO: shouldn't this just be an array, too?
    protected List<Device> modelSpaceDevices;
    protected int[] addressSpaceDeviceMap; // -1 means "not patched"

    // TODO: custom exception?
    public PatchSheet(List<Device> modelSpaceDevices) throws IllegalArgumentException {
        // TODO: convert to an array?
        this.modelSpaceDevices = modelSpaceDevices;

        // Establish the range and mapping of OPC addresses
        int maxAddr = Integer.MIN_VALUE;
        for(Device device: modelSpaceDevices) {
            // TODO: shouldn't device just use int for addr?
            int addr = device.getAddr();
            if(addr > maxAddr) {
                maxAddr = addr;
            }
        }

        addressSpaceDeviceMap = new int[maxAddr + 1];
        Arrays.fill(addressSpaceDeviceMap, -1);
        int i=0;
        for(Iterator<Device> it = modelSpaceDevices.iterator(); it.hasNext(); i++) {
            int addr = it.next().getAddr();
            if(addressSpaceDeviceMap[addr] != -1) {
                throw new IllegalArgumentException("Address " + addr
                    + " appears more than once in the patch sheet.");
            }
            // My kingdom for a list comprehension!
            addressSpaceDeviceMap[addr] = i;
        }

        // TODO write a self-test here
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
    public int[] getAddressSpaceDeviceMap() {
        return this.addressSpaceDeviceMap;
    }
}
