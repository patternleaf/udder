#!/usr/bin/env sh
curl -X POST -d @level_full.json http://localhost:8080/mixer0/layer1
curl -X POST -d @blue_red_woven.json http://localhost:8080/mixer0/layer1/effect
