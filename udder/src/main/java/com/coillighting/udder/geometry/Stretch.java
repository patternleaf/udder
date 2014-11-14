package com.coillighting.udder.geometry;

import java.awt.geom.Point2D;

/** Distort 2D coordinates using a 4-point quad control polygon.
 *  So far this has been tested only with 2D inputs limited to the unit
 *  square. TODO: test with inputs greater than 1 or less than 0.
 */
public class Stretch {

    /** n: normalized to [0..1.0]. Stretch a normalized value into the range
     *  bounded by [nmin..nmax].
     */
    public static double stretch1D(double n, double nmin, double nmax) {
        return nmin + n * (nmax - nmin);
    }

    /** 32-bit version of stretch1d. */
    public static float stretch1D(float n, float nmin, float nmax) {
        return nmin + n * (nmax - nmin);
    }

    /** b and a: orthogonal coordinates, normalize [0..1.0].
     *  Return a new b given a and b coordinates and high and low stretcher bars
     *  for the b dimension. To stretch x, call this method with a=y and b=x,
     *  and stretcher bars for the x dimension (see stretchXY for example).
     *  To stretch y, call thi method with a=x and b=y, with stretcher bars for
     *  y (the second call in stretchXY).
     */
    public static double stretch2D(double a, double b,
            double low_bmin, double low_bmax,
            double high_bmin, double high_bmax)
    {
        // Balance out the influence of the low and the high stretches on dim b.
        return a * stretch1D(b, high_bmin, high_bmax)
            + (1.0 - a) * stretch1D(b, low_bmin, low_bmax);
    }

    /** 32-bit version of stretch2d. */
    public static float stretch2D(float a, float b,
            float low_bmin, float low_bmax,
            float high_bmin, float high_bmax)
    {
        // Balance out the influence of the low and the high stretches on dim b.
        return a * stretch1D(b, high_bmin, high_bmax)
            + (1.0f - a) * stretch1D(b, low_bmin, low_bmax);
    }

    /** Distort xy given the control polygon consisting of the points sw, nw,
     *  etc. Assume xy is normalized to ([0..1.0], [0..1.0]). For no distortion,
     *  specify the unit square as the control polygon.
     */
    public static Point2D.Double stretchXY(Point2D.Double xy,
            Point2D.Double sw, Point2D.Double se,
            Point2D.Double nw, Point2D.Double ne)
    {
        double x = stretch2D(xy.y, xy.x, sw.x, se.x, nw.x, ne.x);
        double y = stretch2D(xy.x, xy.y, sw.y, nw.y, se.y, ne.y);
        return new Point2D.Double(x, y);
    }

    /** 32-bit version of stretchXY. */
    public static Point2D.Float stretchXY(Point2D.Float xy,
            Point2D.Float sw, Point2D.Float se,
            Point2D.Float nw, Point2D.Float ne)
    {
        float x = stretch2D(xy.y, xy.x, sw.x, se.x, nw.x, ne.x);
        float y = stretch2D(xy.x, xy.y, sw.y, nw.y, se.y, ne.y);
        return new Point2D.Float(x, y);
    }

}

/* TODO: port tests from Python
def stretch1d(n, nmin, nmax):
    """n: normalized to [0..1.0]"""
    return nmin + n * (nmax - nmin)

def stretch2d(a, b, low_bmin, low_bmax, high_bmin, high_bmax):
    """b and a: orthogonal coordinates, normalize [0..1.0].
    Return a new b given a and b.
    """
    # Balance out the influence of the low and the high stretches on dim b.
    return (a * stretch1d(b, high_bmin, high_bmax)
            + (1 - a) * stretch1d(b, low_bmin, low_bmax))

def stretchxy(xy, sw, se, nw, ne):
    """All args are points."""
    for arg in (xy, sw, se, nw, se):
        assert 2 == len(arg)

    x = stretch2d(xy[Y], xy[X], sw[X], se[X], nw[X], ne[X])
    y = stretch2d(xy[X], xy[Y], sw[Y], nw[Y], se[Y], ne[Y])
    return [x, y]

def eq(a, b, tolerance=0.0000001):
    return b - a <= tolerance

def pteq(a, b, tolerance=0.0000001):
    return b[X] - a[X] <= tolerance and b[Y] - a[Y] <= tolerance

def test_stretch1d():
    assert 0.0 == stretch1d(0.0, 0.0, 10.0)
    assert 10.0 == stretch1d(1.0, 1.0, 10.0)
    assert 0.5 == stretch1d(0.0, 0.5, 0.5)
    assert 0.5 == stretch1d(0.25, 0.5, 0.5)
    assert 0.5 == stretch1d(0.5, 0.5, 0.5)
    assert 0.5 == stretch1d(0.75, 0.5, 0.5)
    assert 0.5 == stretch1d(0.5, 0.0, 1.0)
    assert 1.0 == stretch1d(0.5, 0.0, 2.0)
    assert 0.5 == stretch1d(0.25, 0.0, 2.0)
    assert 1.5 == stretch1d(0.75, 0.0, 2.0)
    assert -10.0 == stretch1d(0.0, -10.0, 100.0)
    assert -10.0 == stretch1d(0.5, -20.0, 0.0)
    assert 0.0 == stretch1d(1.0, -11.0, 0.0)
    assert 1.0 == stretch1d(0.0, 1.0, 11.0)

def test_stretch2d():
    sw = (0.0, 0.0)
    se = (9.0, -1.0)
    nw = (-2.0, 5.0)
    ne = (10.0, 15.0)

    def tst(xy, pt):
        x = stretch2d(xy[Y], xy[X], sw[X], se[X], nw[X], ne[X])
        assert eq(x, pt[X]), "received %s != expected %s for %s" % (x, pt[X], pt)

        y = stretch2d(xy[X], xy[Y], sw[Y], nw[Y], se[Y], ne[Y])
        assert eq(y, pt[Y]), "received %s != expected %s for %s" % (y, pt[Y], pt)

    tst((0.0, 0.0), sw)
    tst((1.0, 0.0), se)
    tst((0.0, 1.0), nw)
    tst((1.0, 1.0), ne)

def test_stretchxy():
    sw = (0.0, 0.0)
    se = (9.0, -1.0)
    nw = (-2.0, 5.0)
    ne = (10.0, 15.0)
    tst = lambda x, y: tuple(stretchxy((x, y), sw, se, nw, ne))
    assert pteq(sw, tst(0.0, 0.0))
    assert pteq(se, tst(1.0, 0.0))
    assert pteq(nw, tst(0.0, 1.0))
    assert pteq(ne, tst(1.0, 1.0))

def test_all():
    test_stretch1d()
    test_stretch2d()
    test_stretchxy()

test_all()
*/