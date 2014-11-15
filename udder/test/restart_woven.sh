#!/usr/bin/env sh
# Switch Woven off and on, which should cause it to rewind and start over.
curl -X POST -d @level_off.json http://localhost:8080/mixer0/layer1
curl -X POST -d @level_full.json http://localhost:8080/mixer0/layer1
