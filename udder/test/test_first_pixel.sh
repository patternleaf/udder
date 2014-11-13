#!/usr/bin/env sh
# Light up just the first pixel red, so you can see where the
# patch sheet starts.
curl -X POST -d @first_pixel.json http://localhost:8080/mixer0/layer2/effect
