#!/usr/bin/env sh
# set up the blips (10 copies)
texture=pastel_rainbow
delay=1 # seconds

curl -X POST -d @level_full.json http://localhost:8080/mixer0
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer3/effect
# Wait for the blip to run down the rig a few feet before starting the next.
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer4/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer5/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer6/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer7/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer8/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer9/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer10/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer11/effect
sleep $delay
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer12/effect
