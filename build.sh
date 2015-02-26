#!/bin/bash -e

set -a # make sure the env vars are exported
source <(etc/scripts/allocate-jboss-ports)
exec xvfb-run -a mvn "$@"
