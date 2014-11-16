#!/usr/bin/env sh
# Changes take effect only at the END of the current cue.
curl -X POST -d @shuffler_slow.json http://localhost:8080/mixer0/subscriber0
