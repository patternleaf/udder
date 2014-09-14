package com.coillighting.udder;


import com.coillighting.udder.Device;

/** This class is used as a JSON schema spec and output datatype for the Boon
 *  JsonFactory when it deserializes JSON patch sheets exported from Eric's
 *  visualizer. This is just an intermediate representation. We immediately
 *  convert these PatchElements to Devices after parsing.
 *
 *  Example input consisting of three JSON-serialized PatchElements:
 *  FIXME: convert 'gate' to the more generic 'group'.
 *
 *      [{
 *          "point": [-0.43054129481340775, 0.3497840264671482, -0.30439668544441917],
 *          "gate": 0
 *      }, {
 *          "point": [-0.43054129481340775, 0.3586044964826967, -0.30330178155620713],
 *          "gate": 0
 *      }, {
 *          "point": [-0.43054129481340775, 0.36362235722669783, -0.3026789035512811],
 *          "gate": 1
 *      }]
 *
 *  Like Command and PatchElement, this class is structured for compatibility
 *  with the Boon JsonFactory, which automatically binds JSON bidirectionally
 *  to Java classes which adhere to its preferred (bean-style) layout.
 */
public class PatchElement {

    private double[] point;
    private long gate; // a.k.a. group FIXME - ask Eric

    public PatchElement(double[] point, long gate) {
        this.point = point;
        this.gate = gate;
    }

    /** Given an address, conver this intermediate representation into a full
     *  fledged Udder Device. TODO: work out address mappings from model space
     *  to OPC low-level addr space.
     */
    public Device toDevice(long addr) {
        return new Device(addr, this.gate, this.point[0], this.point[1], this.point[2]);
    }

    public String toString() {
        return "PatchElement(["+this.point[0]+","+this.point[1]+","+this.point[2]+"], "+gate+")";
    }

    public double[] getPoint() {
        return this.point;
    }

    public long getGate() {
        return this.gate;
    }

}
