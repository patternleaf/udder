package com.coillighting.udder.infrastructure;

import com.coillighting.udder.model.Device;

import java.util.ArrayList;
import java.util.List;

/** Data structure representing a 3D point in an Open Pixel Control layout
 *  file. We will serialize this as an element in a JSON array. For details
 *  on this dirt simple file format, see the following URL:
 *
 *      https://github.com/zestyping/openpixelcontrol/tree/master/layouts
 *
 *  Example output consisting of three JSON-serialized OpcLayoutPoints:
 *
 *      [
 *          {"point": [1.32, 0.00, 1.32]},
 *          {"point": [1.32, 0.00, 1.21]},
 *          {"point": [1.32, 0.00, 1.10]}
 *      ]
 *
 *  Like Command and PatchElement, this class is structured for compatibility
 *  with the Boon JsonFactory, which automatically binds JSON bidirectionally
 *  to Java classes which adhere to its preferred (bean-style) layout.
 *
 *  After you serialize your JSON layout, you can use it to run the barebones
 *  OpenGL server that comes with Open Pixel Control. Invoke it like this:
 *
 *      $ path/to/bin/gl_server path/to/udder/udder/conf/opc_layout.json
 *
 *  For the Dairy project, we recommend trying Eric Miller's visualizer:
 *
 *      https://github.com/patternleaf/archway
 */
public class OpcLayoutPoint {

    private double[] point;

    public OpcLayoutPoint(double[] point) {
        if(point == null) {
            throw new NullPointerException("point is null");
        } else if(point.length != 3) {
            throw new IllegalArgumentException("invalid point length " + point.length);
        }
        this.point = point;
    }

    public double[] getPoint() {
        return this.point;
    }

    public void scale(double factor) {
        for(int i=0; i<point.length; i++) {
            point[i] *= factor;
        }
    }

    /** Return a list associating OPC address with spatial coordinates.
     *  The Open Pixel Control's gl_server consumes this list and presents
     *  a very simple 3D view of your show, which you can use as a monitor.
     *  See https://github.com/patternleaf/archway for a fine example of how to
     *  create a much snazzier 3D monitor in your browser.
     */
    public static List<OpcLayoutPoint> createOpcLayoutPoints(PatchSheet patchSheet) {
        // We occasionally skip scaling when debugging.
        final boolean autoscale = true;

        // Shrink the layout, which at the Dairy arrived in inches, to fit the
        // limited viewport of the OPC gl model.
        // TODO: automatically compute a decent scale, since the GL viewport
        // lacks auto zoom -- or implement autozoom for gl_server.
        final double glViewportScale = 3.0;

        final double [] origin = {0.0, 0.0, 0.0};

        final Device[] devices = patchSheet.getModelSpaceDevices();
        final int[] addrMap = patchSheet.getDeviceAddressMap();
        ArrayList<OpcLayoutPoint> points = new ArrayList<OpcLayoutPoint>(addrMap.length);

        // Walk the devices in OPC address order. Position a point per address.
        for(int deviceIndex: addrMap) {
            double[] pt;
            if(deviceIndex < 0) {
                // If a device for an OPC address is not patched, just put that
                // address's pixel on the origin where it won't cause trouble.
                pt = origin.clone();
            } else {
                pt = devices[deviceIndex].getPoint().clone();

                // Flip the z-axis to match the Dairy show's model space to
                // openpixelcontrol's gl_server window.
                // TODO: allow the user decide how axes are arranged here.
                pt[2] *= -1;
            }
            points.add(new OpcLayoutPoint(pt));
        }

        if(autoscale) {
            double modelScale = 0.0;

            // Compute how far we need to scale down the gl_server model to
            // fit the unit cube.
            for(OpcLayoutPoint opcPoint: points) {
                double[] pt = opcPoint.getPoint();
                for(int i=0; i<pt.length; i++) {
                    if(Math.abs(pt[i]) > Math.abs(modelScale)) {
                        modelScale = pt[i];
                    }
                }
            }

            // Scale the output OPC layout to fit the model onscreen.
            for(OpcLayoutPoint opcPoint: points) {
                opcPoint.scale(glViewportScale / modelScale);
            }
        }
        return points;
    }

}
