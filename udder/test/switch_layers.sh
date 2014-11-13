#!/usr/bin/env sh
# Turn on one layer (the first arg) and turn off another (the second).
curl -X POST -d @level_full.json http://localhost:8080/mixer0/layer$1
curl -X POST -d @level_off.json http://localhost:8080/mixer0/layer$2
