package com.coillighting.udder.geometry;

// TODO a Bounds interface?

/** A 3D bounding box.
 *
 *  See notes about ditching JavaFX (and possibly switching
 *  to Toxic) in Point3D.java.
 */
public class BoundingCube {

    protected double minX = 0.0;
    protected double minY = 0.0;
    protected double minZ = 0.0;

    protected double maxX = 0.0;
    protected double maxY = 0.0;
    protected double maxZ = 0.0;

    protected double width = 0.0;
    protected double height = 0.0;
    protected double depth = 0.0;

    /** Create a 3D bounding box with the given location and dimensions. */
    public BoundingCube(double minX, double minY, double minZ,
                        double width, double height, double depth)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.width = width;
        this.height = height;
        this.depth = depth;

        this.maxX = width + minX;
        this.maxY = height + minY;
        this.maxZ = depth + minZ;
    }

    /** Create a 2D bounding box as a BoundingCube in the XY plane. */
    public BoundingCube(double minX, double minY,
                        double width, double height)
    {
        this(minX, minY, 0.0, width, height, 0.0);
    }

    public boolean isEmpty() {
        return width < 0.0 || height < 0.0 || depth < 0.0;
    }

    public boolean contains(double x, double y, double z) {
        if(this.isEmpty()) {
            return false;
        } else {
            return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
        }
    }

    public boolean contains(Point3D pt) {
        if(pt != null) {
            return this.contains(pt.getX(), pt.getY(), pt.getZ());
        } else {
            return false;
        }
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getMaxX() {
        return minX + width;
    }

    public double getMaxY() {
        return minY + height;
    }
}
