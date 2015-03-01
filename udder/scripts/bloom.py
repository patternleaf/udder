#!/usr/bin/env python
"""
Exploratory prototype for Blooming Series|Leaf|Cube|Tesseract effect.
"""

def triangular_root(x):
    """Return float: the triangular root of x (int or float).
    Raise ValueError if x is negative.
    """
    return ((8.0*x + 1.0)**0.5 - 1.0) / 2.0

def discrete_triangular_root(x):
    """Return int: the triangular root of x (int or float), rounded down to the
    nearest integer.
    Raise ValueError if x is negative.
    """
    return int(triangular_root(x))

def test():
    tolerance = 0.00000000001 # floating point slop

    assert triangular_root(0.0) == 0.0
    assert triangular_root(0) == 0.0
    assert discrete_triangular_root(0) == 0

    assert abs(triangular_root(0.5) - 0.61803398875) < tolerance
    assert discrete_triangular_root(0.5) == 0

    assert triangular_root(1.0) == 1.0
    assert triangular_root(1) == 1.0
    assert discrete_triangular_root(1.0) == 1
    assert discrete_triangular_root(1) == 1

    assert abs(triangular_root(1.5) - 1.30277563773) < tolerance
    assert discrete_triangular_root(1.5) == 1

    assert abs(triangular_root(2.0) - 1.56155281281) < tolerance
    assert abs(triangular_root(2) - 1.56155281281) < tolerance
    assert discrete_triangular_root(2.0) == 1
    assert discrete_triangular_root(2) == 1

    assert triangular_root(3.0) == 2.0
    assert triangular_root(3) == 2.0
    assert discrete_triangular_root(3.0) == 2
    assert discrete_triangular_root(3) == 2

    assert triangular_root(6.0) == 3.0
    assert triangular_root(6) == 3.0
    assert discrete_triangular_root(6.0) == 3
    assert discrete_triangular_root(6) == 3

    assert triangular_root(10.0) == 4.0
    assert triangular_root(10) == 4.0

    assert triangular_root(15.0) == 5.0
    assert triangular_root(15) == 5.0

    assert abs(triangular_root(18.0) - 5.5207972894) < tolerance
    assert abs(triangular_root(18) - 5.5207972894) < tolerance

    assert triangular_root(21.0) == 6.0
    assert triangular_root(21) == 6.0

    try:
        triangular_root(-1.0)
        assert False, "Expected ValueError."
    except ValueError:
        pass

test()
print "Test complete."
