package com.coillighting.udder;

import java.awt.geom.Point2D;

import com.coillighting.udder.Stretch;

/** A simple public datastructure for storing four
 *  points of a control polygon.
 */
public class ControlQuad {

    // Distortion quad for Stretch.stretchXY
    public Point2D.Double sw = null;
    public Point2D.Double se = null;
    public Point2D.Double nw = null;
    public Point2D.Double ne = null;

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

    public Point2D.Double stretchXY(Point2D.Double xy) {
        return Stretch.stretchXY(xy, sw, se, nw, ne);
    }
}
