#!/usr/bin/env python
"""Format a base 10 integer string as the components of an RGB 24 bit pixel.

Test cases:

Red @ 255 + blue @ 254: 16711934
Green @ 255 + blue @ 253: 65533
Blue: 255
Green @ 1: 256
Green @ 1 + Blue @ 255: 511

"""
import sys

i = int(sys.argv[1])

if i < 0:
    raise ValueError("Cannot convert the negative number %r to a pixel." % i)

s = hex(i)
print "Input", s

pad = 8 - len(s)
s = ("0x" + ("0" * pad)) + s[2:]
if len(s) != 8:
    raise ValueError("Cannot convert %r (%r) into an RGBA pixel." % (i, s))

rs = s[2:4]
ri = int(rs, 16)

gs = s[4:6]
gi = int(gs, 16)

bs = s[6:8]
bi = int(bs, 16)

print "R", rs, ri
print "G", gs, gi
print "B", bs, bi
