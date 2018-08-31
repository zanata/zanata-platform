#!/usr/bin/env bash
set -eu

# TODO
# Implement rundev.sh options:
#      -e for smtp server
#      -l for smtp login
#      -p for port offset
#      -n for docker network
# Add Zanata MT integration


# determine directory containing this script: https://stackoverflow.com/a/246128/14379
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"

cd $DIR

ZANATA_WAR=$(echo $PWD/../zanata-war/target/zanata-*.war)
if ! [ -f "$ZANATA_WAR" ]
then
    echo "===== NO war file found (or more than one). Please build Zanata war first (or delete old versions) ====="
    exit 1
fi

set -x

# make config scripts available inside Docker
mkdir -p target
cp -a ../etc/scripts/jboss-cli-jjs ../etc/scripts/configure-app-server.js target/

docker build --tag zanata/server-dev --file Dockerfile .

# First transform flattens everything
# Second transform renames the versioned zanata war to ROOT.war
tar --create \
  Dockerfile.zanata \
  ${ZANATA_WAR} \
  --transform 's,.*/,,' \
  --transform 's/zanata-.*\.war/ROOT.war/' |
    docker build -t zanata-dev --file Dockerfile.zanata -

docker-compose up
