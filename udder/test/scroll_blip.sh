#!/usr/bin/env sh
watch -n 0.10 curl -X POST -d @blip.json http://localhost:8080/mixer0/layer3/effect
