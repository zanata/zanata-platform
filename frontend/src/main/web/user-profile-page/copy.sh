#!/bin/bash


arg1=$1
arg2=$2

srcDest=${arg1:="$HOME/work/root/server/zanata-war/src/main/webapp/profile/js/"}
deployedDest=${arg2:="/NotBackedUp/tools/jboss-eap/standalone/deployments/zanata.war/profile/js/"}

echo "will copy bundle.min.js to [$srcDest] and [$deployedDest]"

cp bundle.min.js ${srcDest}
cp bundle.min.js ${deployedDest}

