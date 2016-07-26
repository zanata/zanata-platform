#!/bin/bash
#set -x

function usage {
  echo ">> Run this script to quickly build the Zanata web archive" >&2
  echo ">>>> -c Only build GWT components for Chrome (incompatible with -f)" >&2
  echo ">>>> -f Only build GWT components for Firefox (incompatible with -c)" >&2
  echo ">>>> Default GWT build is both Chrome and Firefox" >&2
  exit 0;
}

gwtMode="-Dchromefirefox"
while getopts "cfh" opt; do
  case ${opt} in
    c)
      if [ $gwtMode == "-Dfirefox" ]
        then usage
      fi
      gwtMode="-Dchrome"
      echo ">> Building GWT for Google Chrome"
      ;;
    f)
      if [ $gwtMode == "-Dchrome" ]
        then usage
      fi
      gwtMode="-Dfirefox"
      echo ">> Building GWT for Mozilla Firefox"
      ;;
    h)
      usage
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1;
      ;;
  esac
done

DIR="$( cd -P "$( dirname "$0" )" && pwd )"
cd $DIR/../..
mvn -DskipArqTests -DskipUnitTests -Danimal.sniffer.skip $gwtMode -am -pl zanata-war package install
