package com.coillighting.udder.model;

import com.coillighting.udder.geometry.BoundingCube;
import com.coillighting.udder.geometry.Point3D;

/** A logical Device such as a cluster of LEDs that are animated as a single
 *  pixel. See also notes in Patchable.java.
 */
public class Device extends Object {

    /** This Device's address in some arbitrary address space. For the dairy,
     * this address is in the space of a single OPC channel.
     *
     * FUTURE Multiple channels/universes.
     *
     * Not public because no effects should be computed on the basis of a
     * Device's address.
     */
    protected int addr = 0;

    /** A dirt simple grouping mechanism. Each Device belongs to exactly one
     *  group (for now). For the Dairy installation, this will indicate gate
     *  0 or gate 1, in case an Animator cares which group the Device is in.
     *
     * Public because device group computations happen in the hottest inner loop.
     */
    public int group=0;

    /** Position in model space. Public because device positional
     *  computations happen in the hottest inner loop.
     */
    public double x = 0.0;
    public double y = 0.0;
    public double z = 0.0;

    public Device(int addr, int group, double x, double y, double z) {
        if(addr < 0) {
            throw new IllegalArgumentException("Invalid Device address: " + addr);
        } else if(group < 0) {
            throw new IllegalArgumentException("Negative group index: " + addr);
        }
        this.addr = addr;
        this.group = group;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "Device @"+addr+" ["+group+"] ("+x+","+y+","+z+")";
    }

    public int getAddr() {
        return this.addr;
    }

    public int getGroup() {
        return this.group;
    }

    // TODO refactor x, y and z as a double[] for consistency with other classes? or refactor as a Point3D (see below). We should standardize here.
    public double[] getPoint() {
        return new double[]{x, y, z};
    }

    public Point3D getPoint3D() {
        return new Point3D(x, y, z);
    }

    /** Return the 3D bounding box for the given devices. */
    public static BoundingCube getDeviceBoundingCube(Device[] devices) {
        if(devices==null) {
            return null;
        } else {
            // FIXME: determine the real min and max values that will compare properly.
            // A seed value of Double.MAX_VALUE did not work with all comparisons here. WTF?
            // For now you must place your devices within this cube, sorry:
            final double MAX_VALUE = 999999999.0;
            final double MIN_VALUE = -999999999.0;

            double minx=MAX_VALUE;
            double maxx=MIN_VALUE;
            double miny=MAX_VALUE;
            double maxy=MIN_VALUE;
            double minz=MAX_VALUE;
            double maxz=MIN_VALUE;

            for(Device d: devices) {
                double x = d.x;
                double y = d.y;
                double z = d.z;
                if(x < minx) minx = x;
                if(x > maxx) maxx = x;
                if(y < miny) miny = y;
                if(y > maxy) maxy = y;
                if(z < minz) minz = z;
                if(z > maxz) maxz = z;
            }
            return new BoundingCube(minx, miny, minz,
                maxx-minx, maxy-miny, maxz-minz);
        }
    }
}
