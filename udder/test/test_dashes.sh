#!/usr/bin/env sh
# Display full intensity RGB dashes so you can see roughly
# how your patch sheet maps to the rig.
curl -X POST -d @dashes.json http://localhost:8080/mixer0/layer2/effect
