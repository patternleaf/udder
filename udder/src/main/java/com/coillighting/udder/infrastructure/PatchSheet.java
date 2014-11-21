package com.coillighting.udder.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import com.coillighting.udder.model.Device;
import com.coillighting.udder.util.FileUtil;
import org.boon.json.JsonFactory;

/** Organize devices so that we can traverse them either in OPC address order
 *  or in patch sheet order. Patch sheet order is arbitrary, but most LDs
 *  like to patch their show so that the order of the devices on the patch
 *  sheet correlate spatially with the the order of the devices in space.
 */
public class PatchSheet {

    protected Device[] modelSpaceDevices;

    /** Encode {opc_addr: device_index} as an array. Index (key) is OPC address,
     *  value is index in modelSpaceDevices, if any. A value of -1 means "not
     *  patched." Otherwise the value must be >= 0.
     */
    protected int[] deviceAddressMap;

    public PatchSheet(List<Device> modelSpaceDevices) throws DeviceAddressException
    {
        this.modelSpaceDevices = modelSpaceDevices.toArray(
                new Device[modelSpaceDevices.size()]);

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

        // Sort modelSpaceDevices by OPC address, filling in gaps with the -1
        // placeholder. Store the reordered list by index in deviceAddressMap.
        int i=0;
        for(Iterator<Device> it = modelSpaceDevices.iterator(); it.hasNext(); i++) {
            int addr = it.next().getAddr();
            if(deviceAddressMap[addr] != -1) {
                throw new DeviceAddressException("Address " + addr
                    + " appears more than once in the patch sheet.");
            }
            deviceAddressMap[addr] = i;
        }
    }

    /** Return a shallow copy of the array of Devices in the order originally
     * specified by the user.
     *
     * By convention the patch sheet ordering of Devices should correspond
     * somehow with how the physical devices are arranged in space.
     */
    public Device[] getModelSpaceDevices() {
        return this.modelSpaceDevices.clone();
    }

    /** Return a shallow copy of this patch sheet's index of devices.
     * The index is schematized such that for any OPC address n, the device at
     * devices[n] has the address n. If there is only null at devices[n],
     * then no device was patched to that address.
     */
    public int[] getDeviceAddressMap() {
        return this.deviceAddressMap.clone();
    }

    /** Parse the JSON patch sheet as an array of Devices wrapped in a
     * PatchSheet.
     */
    public static PatchSheet parsePatchSheet(String configPath)
            throws DeviceAddressException, IOException {

        // When you need to debug JSON parsing, these two calls help:
        // byte[] encoded = Files.readAllBytes(Paths.get(configPath));
        // String json = new String(encoded, StandardCharsets.UTF_8);

        List<PatchElement> patchElements = JsonFactory.fromJsonArray(
                FileUtil.fileToString(configPath), PatchElement.class);

        List<Device> devices = new ArrayList<Device>(patchElements.size());
        for(PatchElement pe: patchElements) {
            devices.add(pe.toDevice());
        }
        return new PatchSheet(devices);
    }

}
