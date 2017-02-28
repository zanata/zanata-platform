#!/bin/bash -eu
if [ $# -lt 1 ];then
    echo "Usage: $0 <command> [Command Args] ... " > /dev/stderr
    exit 1
fi
Cmd=$1
shift

CmdExitStatus=0
# execute the command
$Cmd ${@-} || CmdExitStatus=$?

# remove stray processes
if pkill -P $$; then
    ## pkill returns zero if processes killed
    echo "killed stray process" > /dev/stderr
fi

exit $CmdExitStatus
