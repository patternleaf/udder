#!/usr/bin/env sh
# set up the blips (10 copies)
texture=pastel_rainbow

# Load the texture for this layer
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer3/effect
# Run the blip down the rig a few feet
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer4/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer5/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer6/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done
curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer7/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer8/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer9/effect
for i in {1..100}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done

curl -X POST -d @${texture}.json http://localhost:8080/mixer0/layer10/effect

#start animating forever, as fast as possible (very slow with `watch`)
#watch -n 0.01 curl -X POST -d @level_full.json http://localhost:8080/mixer0

# or do it a lot faster, but not forever: skip watch and use the bash loop
for i in {1..10000000}; do curl -X POST -d @level_full.json http://localhost:8080/mixer0; done
