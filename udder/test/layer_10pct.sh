#!/usr/bin/env sh
curl -X POST -d @level_10.json http://localhost:8080/mixer0/layer$1
