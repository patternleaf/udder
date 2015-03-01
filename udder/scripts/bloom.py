#!/usr/bin/env python
"""
Exploratory prototype for Blooming Series|Leaf|Cube|Tesseract effect.
"""

def triangular_number(n):
    return ((2.0*n + 1.0)**2.0 - 1.0) / 8.0

def triangular_root(x):
    """Return float: the triangular root of x (int or float).
    Raise ValueError if x is negative.
    """
    return ((8.0*x + 1.0)**0.5 - 1.0) / 2.0

def oscillate_triangular_root_color(offset, scale, palette):
    """Map the pixel at offset to a thread colors (where color cycle frequency
    = len(palette)), given some scale multiplier (in space per thread).

    offset(float): In pixels. This is the spatial location of the pixel you
    want to color.

    frequency (float): 2.0 for binary blinker, 3 for three-way, ...

    scale (float): distince in pixels allocated to the spatial range
        corresponding to x in [1, 2.0).

    Impl schematic:
         | dxnorm ||||||||||||||||||||||
        x0------->x1
        x0----------------------------->x2
        n0                              n1
    """
    x1  = offset / scale
    n0 = float(int(triangular_root(x1)))
    x0 = triangular_number(n0)
    n1 = n0 + 1.0
    x2 = triangular_number(n1)
    dxnorm = (x1 - x0) / (x2 - x0)
    # print "x1 %s  x0 %s  x2 %s = dxnorm %s" % (x1, x0, x2, dxnorm)
    frequency = len(palette) # int

    color_index_float = dxnorm * float(frequency)
    # because 3.0 * 0.333333333 is rounding down
    if color_index_float % 1 > 0.999999999998:
        color_index_float += 0.01 # bump it up to the next color if it's very close in order to make up for floating point imprecision

    # print "color_index_float = %s => %s" % (color_index_float, int(color_index_float))

    color_index = int(color_index_float) # int
    # print "int(dxnorm %s * float(frequency %s)) = color_index %s" % (dxnorm, frequency, color_index)
    # print "color_index @%s[x%s] (before) %s" % (offset, frequency, color_index)
    if color_index >= frequency:
        color_index = frequency - 1
    # print "    color_index @%s[x%s] (after) %s" % (offset, frequency, color_index)
    return palette[color_index]

def test():
    tolerance = 0.00000000001 # floating point slop

    assert triangular_root(0.0) == 0.0
    assert triangular_root(0) == 0.0

    assert abs(triangular_root(0.5) - 0.61803398875) < tolerance

    assert triangular_root(1.0) == 1.0
    assert triangular_root(1) == 1.0

    assert abs(triangular_root(1.5) - 1.30277563773) < tolerance

    assert abs(triangular_root(2.0) - 1.56155281281) < tolerance
    assert abs(triangular_root(2) - 1.56155281281) < tolerance

    assert triangular_root(3.0) == 2.0
    assert triangular_root(3) == 2.0

    assert triangular_root(6.0) == 3.0
    assert triangular_root(6) == 3.0

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

    # verify inverse operations
    xs = (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
    ns = (0.0, 1.0, 3.0, 6.0, 10.0, 15.0, 21.0, 28.0, 36.0, 45.0, 55.0, 66.0, 78.0, 91.0, 105.0, 120.0, 136.0, 153.0)
    for i, x in enumerate(xs):
        n = triangular_number(x)
        assert n - ns[i] < tolerance, "at [%s]: %s !~= %s" % (i, n, ns[i])
        assert triangular_root(n) - x < tolerance

test()
print "Test complete."

xs = tuple(xrange(18))
if True:
    print ''.join(str(x).ljust(6) for x in xs)
    print ''.join(str(round(triangular_root(x),3)).ljust(6) for x in xs)
    print ''.join(str(round(triangular_number(n),3)).ljust(6) for n in xs)
    print ''.join(str(round(triangular_root(triangular_number(n)),3)).ljust(6) for n in xs)

if True:
    palette1 = ['a']
    print ''.join(oscillate_triangular_root_color(x, 1.0, palette1).ljust(6) for x in xs)

    palette2 = ['a', 'b']
    print ''.join(oscillate_triangular_root_color(x, 1.0, palette2).ljust(6) for x in xs)
    osc2 = ''.join(oscillate_triangular_root_color(x, 2.0, palette2) for x in tuple(xrange(42)))
    expected2 = "abaabbaaabbbaaaabbbbaaaaabbbbbaaaaaabbbbbb"
    assert expected2 == osc2, "\n%r\n%r" % (expected2, osc2)

    palette3 = ['a', 'b', 'c']
    print ''.join(oscillate_triangular_root_color(x, 3.0, palette3).ljust(6) for x in xs)
    osc3 = ''.join(oscillate_triangular_root_color(float(x), 3.0, palette3) for x in tuple(xrange(63)))
    expected3 = "abcaabbccaaabbbcccaaaabbbbccccaaaaabbbbbcccccaaaaaabbbbbbcccccc"
    ruler1 = ''.join(str(i % 10) for i in xrange(len(expected3)))
    ruler10 = ''.join(str(int(i/10)) for i in xrange(len(expected3)))
    assert expected3 == osc3, "\nruler     %s\nruler     %s\nexpected %r !=\nreceived %r" \
        % (ruler10, ruler1, expected3, osc3)

    palette4 = ['a', 'b', 'c', 'd']
    print ''.join(oscillate_triangular_root_color(x, 4.0, palette4).ljust(6) for x in xs)
    osc4 = ''.join(oscillate_triangular_root_color(float(x), 4.0, palette4) for x in tuple(xrange(84)))
    expected4 = "abcdaabbccddaaabbbcccdddaaaabbbbccccddddaaaaabbbbbcccccdddddaaaaaabbbbbbccccccdddddd"
    ruler1 = ''.join(str(i % 10) for i in xrange(len(expected4)))
    ruler10 = ''.join(str(int(i/10)) for i in xrange(len(expected4)))
    assert expected4 == osc4, "\nruler     %s\nruler     %s\nexpected %r !=\nreceived %r" \
        % (ruler10, ruler1, expected4, osc4)


    # freq > len(palette)
    freq = 8.0
    print ''.join(oscillate_triangular_root_color(x, freq, palette4).ljust(6) for x in xs)
    osc4x2 = ''.join(oscillate_triangular_root_color(float(x), freq, palette4) for x in tuple(xrange(84*2)))
    expected4x2 = "aabbccddaaaabbbbccccddddaaaaaabbbbbbccccccddddddaaaaaaaabbbbbbbbccccccccddddddddaaaaaaaaaabbbbbbbbbbccccccccccddddddddddaaaaaaaaaaaabbbbbbbbbbbbccccccccccccdddddddddddd"
    ruler1 = ''.join(str(i % 10) for i in xrange(len(expected4x2)))
    ruler10 = ''.join(str(int(i/10)) for i in xrange(len(expected4x2)))
    assert expected4x2 == osc4x2, "\nruler     %s\nruler     %s\nexpected %r !=\nreceived %r" \
        % (ruler10, ruler1, expected4x2, osc4x2)

    # freq < len(palette)
    freq = 2.0
    print ''.join(oscillate_triangular_root_color(x, freq, palette4).ljust(6) for x in xs)
    osc4d2 = ''.join(oscillate_triangular_root_color(float(x), freq, palette4) for x in tuple(xrange(84/2)))
    # every 2nd b and d get skipped, predictably
    expected4d2 = "acabcdaabccdaabbccddaaabbcccddaaabbbcccddd"
    ruler1 = ''.join(str(i % 10) for i in xrange(len(expected4d2)))
    ruler10 = ''.join(str(int(i/10)) for i in xrange(len(expected4d2)))
    assert expected4d2 == osc4d2, "\nruler     %s\nruler     %s\nexpected %r !=\nreceived %r" \
        % (ruler10, ruler1, expected4d2, osc4d2)

    palette5 = ['a', 'b', 'c', 'd', 'e']
    print ''.join(oscillate_triangular_root_color(x, 5.0, palette5).ljust(6) for x in xs)
    osc5 = ''.join(oscillate_triangular_root_color(float(x), 5.0, palette5) for x in tuple(xrange(105)))
    expected5 = "abcdeaabbccddeeaaabbbcccdddeeeaaaabbbbccccddddeeeeaaaaabbbbbcccccdddddeeeeeaaaaaabbbbbbccccccddddddeeeeee"
    ruler1 = ''.join(str(i % 10) for i in xrange(len(expected5)))
    ruler10 = ''.join(str(int(i/10)) for i in xrange(len(expected5)))
    assert expected5 == osc5, "\nruler     %s\nruler     %s\nexpected %r !=\nreceived %r" \
        % (ruler10, ruler1, expected5, osc5)

assert oscillate_triangular_root_color(21.0, 3.0, palette3) == 'a'
assert oscillate_triangular_root_color(22.0, 3.0, palette3) == 'b'
assert oscillate_triangular_root_color(23.0, 3.0, palette3) == 'b'
