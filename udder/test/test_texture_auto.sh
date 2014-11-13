#!/usr/bin/env sh
curl -X POST -d @texture_auto.json http://localhost:8080/mixer0/layer$1/effect
