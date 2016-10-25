#!/bin/bash -eu
Cmd=$1
shift
$Cmd ${@-}
pkill -P $$
