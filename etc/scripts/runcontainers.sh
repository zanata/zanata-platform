#!/bin/bash
#set -x

clean=clean
extraArgs=
while getopts "nhd:" opt; do
  case ${opt} in
    n)
      echo ">> -n specified. Will NOT run maven goal 'clean'." >&2
      clean=''
      ;;
    h)
      echo ">> run this script to prepare a functional test war and start the app and db containers" >&2
      echo ">>>> -n if you don't want to run maven clean goal. Useful when you have run functional-test-db.snapshot.sh." >&2
      echo ">>>> -d <extra maven arguments, e.g. '-Dwebdriver.type=firefox -Dsmtp.port=25'> if you want to pass in extra arguments when running functional test" >&2
      exit 0;
      ;;
    d)
      extraArgs=$OPTARG
      echo ">> extra parameter passed to functional-test module execution:$extraArgs"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1;
      ;;
  esac
done

scriptBaseDir=$(dirname $0)
serverModuleRoot=$(readlink -f "${scriptBaseDir}/../..")

cd ${serverModuleRoot}

echo ">> Now working in $(pwd)"
echo

# this will build zanata war and prepare war overlay for functional test
mvn ${clean} package -Dchromefirefox -DskipTests -Dappserver=wildfly8 -pl zanata-model,zanata-war,zanata-test-war;

# this will start the app and db containers and deploy above generated overlay war
mvn ${clean} package -Dappserver=wildfly8 -pl functional-test ${extraArgs};
