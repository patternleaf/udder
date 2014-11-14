package com.coillighting.udder.infrastructure;

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
 *  For the Dairy project, we recommend using Eric's visualizer:
 *
 *      https://github.com/patternleaf/archway
 *
 *  TODO: move point serialization here.
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

}
