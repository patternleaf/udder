package com.coillighting.udder;

import java.awt.geom.Point2D;

// Float impl for 32 bit processors. TODO: 64 bit double support when needed.
public class Stretch {

    /** n: normalized to [0..1.0] */
    public static float stretch1d(float n, float nmin, float nmax) {
        return nmin + n * (nmax - nmin);
    }

    /** b and a: orthogonal coordinates, normalize [0..1.0].
     *  Return a new b given a and b coordinates and high and low stretcher bars
     *  for the b dimension.
     */
    public static float stretch2d(float a, float b,
            float low_bmin, float low_bmax,
            float high_bmin, float high_bmax)
    {
        // Balance out the influence of the low and the high stretches on dim b.
        return a * stretch1d(b, high_bmin, high_bmax)
            + (1.0f - a) * stretch1d(b, low_bmin, low_bmax);
    }

    /** Distort xy given the control polygon consisting of the points sw, nw,
     *  etc. Assume xy is normalized to ([0..1.0], [0..1.0]). For no distortion,
     *  specify the unit square as the control polygon.
     */
    public static Point2D.Float stretchxy(Point2D.Float xy,
            Point2D.Float sw, Point2D.Float se,
            Point2D.Float nw, Point2D.Float ne)
    {
        return new Point2D.Float(stretch2d(xy.y, xy.x, sw.x, se.x, nw.x, ne.x),
                                 stretch2d(xy.x, xy.y, sw.y, nw.y, se.y, ne.y));
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