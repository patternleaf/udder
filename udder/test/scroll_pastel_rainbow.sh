#!/usr/bin/env sh
# Test pattern: scroll a pastel rainhow across the rig in patch sheet order.
curl -X POST -d @pastel_rainbow.json http://localhost:8080/mixer0/layer20/effect
