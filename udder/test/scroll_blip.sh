#!/usr/bin/env sh
# Test pattern: scroll an orange blip across the rig in patch sheet order.
curl -X POST -d @level_full.json http://localhost:8080/mixer0/layer19
curl -X POST -d @blip.json http://localhost:8080/mixer0/layer19/effect
