#!/usr/bin/env sh
curl -X POST -d @level_full.json http://localhost:8080/mixer0/layer$1
curl -X POST -d @texture_manual.json http://localhost:8080/mixer0/layer$1/effect
