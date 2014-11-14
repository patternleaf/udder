package com.coillighting.udder.geometry;

import java.awt.geom.Point2D;

/** A simple public datastructure for storing four
 *  points of a control polygon.
 */
public class ControlQuad {

    // Distortion quad for Stretch.stretchXY
    public Point2D.Double sw = null;
    public Point2D.Double se = null;
    public Point2D.Double nw = null;
    public Point2D.Double ne = null;

    public ControlQuad(double[][] corners) {
        this.reset();
        this.setCorners(corners);
    }

    /** Set this ControlQuad to the given four points.
     *  Incorporate the four values by copy.
     */
    public ControlQuad(Point2D.Double sw, Point2D.Double se,
                       Point2D.Double nw, Point2D.Double ne)
    {
        this.reset();
        this.setCorners(sw, se, nw, ne);
    }

    public ControlQuad() {
        this.reset();
    }

    public void setCorners(double[][] corners) {
        if(corners != null) {
            if(corners.length != 4) {
                throw new IllegalArgumentException("A ControlQuad requires exactly 4 corners.");
            } else {
                final int X = 0;
                final int Y = 1;
                this.sw.x = corners[0][X];
                this.sw.y = corners[0][Y];
                this.se.x = corners[1][X];
                this.se.y = corners[1][Y];
                this.nw.x = corners[2][X];
                this.nw.y = corners[2][Y];
                this.ne.x = corners[3][X];
                this.ne.y = corners[3][Y];
            }
        }
    }

    public void setCorners(Point2D.Double sw, Point2D.Double se,
                           Point2D.Double nw, Point2D.Double ne)
    {
        this.sw.x = sw.x;
        this.sw.y = sw.y;
        this.se.x = se.x;
        this.se.y = se.y;
        this.nw.x = nw.x;
        this.nw.y = nw.y;
        this.ne.x = ne.x;
        this.ne.y = ne.y;

    }

    /** Set this ControlQuad to the unit square, which
     *  encodes the identity transform for StretchXY.
     *  (Re)allocate all four points.
     */
    public void reset() {
        sw = new Point2D.Double(0.0, 0.0);
        se = new Point2D.Double(1.0, 0.0);
        nw = new Point2D.Double(0.0, 1.0);
        ne = new Point2D.Double(1.0, 1.0);
    }

    /** Take the other's corner values and set them on this ControlQuad.
     *  If the other sends a value as null instead of a double, ignore
     *  it and keep the old value. Use this to eliminate boilerplate from the
     *  simple act of keeping this ControlQuad in a valid state for stretching.
     */
    public void setDoubleValues(ControlQuad other) {
        if(other != null) {
            if(other.sw != null) {
                sw.x = other.sw.x;
                sw.y = other.sw.y;
            }
            if(other.se != null) {
                se.x = other.se.x;
                se.y = other.se.y;
            }
            if(other.nw != null) {
                nw.x = other.nw.x;
                nw.y = other.nw.y;
            }
            if(other.ne != null) {
                ne.x = other.ne.x;
                ne.y = other.ne.y;
            }
        }
    }

    public Point2D.Double stretchXY(Point2D.Double xy) {
        return Stretch.stretchXY(xy, sw, se, nw, ne);
    }

    /** In JSON-compatible format. */
    public String toString() {
        return "[["+sw.x+","+sw.y+"],["+se.x+","+se.y+"],["+nw.x+","+nw.y+"],["+ne.x+","+ne.y+"]]";
    }
}
