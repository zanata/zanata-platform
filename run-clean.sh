#!/bin/bash -eu
if [ $# -lt 1 ];then
    echo "Usage: $0 <command> [Command Args] ... " > /dev/stderr
    exit 1
fi
Cmd=$1
shift
$Cmd ${@-}
if pgrep -P $$ -f $Cmd ${@-} ;then
    ## Only kill children if they exist, otherwise pkill return non zero
    pkill -P $$
fi

