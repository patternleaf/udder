package com.coillighting.udder.geometry;

/** A basic fast-access data structure for a high resolution 3D point.
 *  At first we tried using the JavaFX Point3D, but users reported
 *  widespread problems loading the optional JavaFX jar, which lives
 *  in a system-specific location that Maven has a hard time finding
 *  (even Maven's JavaFX plugin can't always find it).
 *
 *  TODO: We should probably either convert to the Toxic Vec3D or
 *  flesh out this API. For now this is just enough to get started
 *  with a dirt simple 3D struct.
 */
public class Point3D {

    public double x = 0.0;
    public double y = 0.0;
    public double z = 0.0;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final double getZ() {
        return z;
    }

    public final void setX(double x) {
        this.x = x;
    }

    public final void setY(double y) {
        this.y = y;
    }

    public final void setZ(double z) {
        this.z = z;
    }

}
