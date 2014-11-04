package com.coillighting.udder.geometry;

/** A basic fast-access data structure for a high resolution 3D point.
 *  At first we tried using the JavaFX Point3D, but users reported
 *  widespread problems loading the optional JavaFX jar, which lives
 *  in a system-specific location that Maven has a hard time finding
 *  (even Maven's JavaFX plugin).
 *
 *  TODO: We should probably either convert to the Toxic Vec3D or
 *  flesh out this API. For now this is just enough to get started
 *  with a simple 3D struct.
 */
public class Point3D {

    // TODO: profile speed of public access vs. JIT-optimized getter method access.
    // Public access might be equivalent once dynamically optimized, but it
    // might still be faster, and it sure as hell is easier to type.
    protected double x = 0.0;
    protected double y = 0.0;
    protected double z = 0.0;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

}