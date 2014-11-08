#!/usr/bin/env python
"""Format a base 10 integer string as the components of an RGBA 32 bit pixel.

Test cases:

Red + full alpha: 4278190335
Green + full alpha: 16711935
Blue  + full alpha: 65535
Black  + full alpha: 255
Blue @ 1 + zero alpha: 256
Blue @ 1 + full alpha: 511
"""
import sys

i = int(sys.argv[1])

if i < 0:
    raise ValueError("Cannot convert the negative number %r to a pixel." % i)

s = hex(i)
print "Input", s

pad = 10 - len(s)
s = ("0x" + ("0" * pad)) + s[2:]
if len(s) != 10:
    raise ValueError("Cannot convert %r (%r) into an RGBA pixel." % (i, s))

rs = s[2:4]
ri = int(rs, 16)

gs = s[4:6]
gi = int(gs, 16)

bs = s[6:8]
bi = int(bs, 16)

alpha_s = s[8:10]
alpha_i = int(alpha_s, 16)

print "R", rs, ri
print "G", gs, gi
print "B", bs, bi
print "A", alpha_s, alpha_i
