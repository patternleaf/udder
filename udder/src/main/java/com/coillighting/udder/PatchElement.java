package com.coillighting.udder;


import com.coillighting.udder.Device;

/** This class is used as a JSON schema spec and output datatype for the Boon
 *  JsonFactory when it deserializes JSON patch sheets exported from Eric's
 *  visualizer. This is just an intermediate representation. We immediately
 *  convert these PatchElements to Devices after parsing.
 *
 *  Example input consisting of three JSON-serialized PatchElements:
 *
 *      [{
 *          "point": [-111, 92.33984355642154, -78.20986874321204],
 *          "group": 0,
 *          "address": 57
 *      }, {
 *          "point": [-111, 93.58520914575311, -78.05527879900943],
 *          "group": 0,
 *          "address": 56
 *      }, {
 *          "point": [-111, 94.70067095432645, -77.91681404627441],
 *          "group": 0,
 *          "address": 55
 *      }]
 *
 *  Like Command and PatchElement, this class is structured for compatibility
 *  with the Boon JsonFactory, which automatically binds JSON bidirectionally
 *  to Java classes which adhere to its preferred (bean-style) layout.
 */
public class PatchElement {

    private double[] point;
    private int group;
    private int address;

    // TODO validate parameter ranges
    public PatchElement(double[] point, int group, int address) {
        this.point = point;
        this.group = group;
        this.address = address;
    }

    // TEMP
    private double getZ() {
        double z;
        if(this.point != null && this.point.length == 2) {
            // The Z-axis disappeared, so for now just set Z to a multiple of group.
            // FIXME this 15' offset is hardcoded for the dairy, no good for other shows.
            z = (15.0 * 12.0) * (double) group;
        } else if(this.point != null && this.point.length == 3) {
            z = this.point[2];
        } else {
            throw new IllegalArgumentException("Udder does not support "
                + this.point.length + "-dimensional shows.");
        }
        return z;
    }

    /** Convert this intermediate representation into a full-fledged Udder Device.
     */
    public Device toDevice() {
        double z = this.getZ(); // simultaneously validate point
        return new Device(this.address, this.group, this.point[0], this.point[1], z);
    }

    public String toString() {
        double z = this.getZ(); // simultaneously validate point
        return "PatchElement([" + point[0] + "," + point[1] + "," + z
            + "], " + group + ")";
    }

    public double[] getPoint() {
        return this.point;
    }

    public int getGroup() {
        return this.group;
    }

}
