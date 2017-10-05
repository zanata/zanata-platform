#!/bin/bash

# determine directory containing this script
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(realpath "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cd $DIR

docker build --tag wildfly-zanata-base .

# First transform flattens everything
# Second transform renames the versioned zanata war to ROOT.war
tar --create \
  Dockerfile.zanata \
  ../zanata-war/target/zanata-*.war \
  --transform 's,.*/,,' \
  --transform 's/zanata-.*\.war/ROOT.war/' |
    docker build -t zanata-dev --file Dockerfile.zanata -

docker-compose up
