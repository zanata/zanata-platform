#!/bin/bash

docker build --tag wildfly-zanata-base .

# First transform flattens everything
# Second transform removes the version in zanata.war
tar --create \
  Dockerfile.zanata \
  ../zanata-war/target/zanata-*.war \
  --transform 's,.*/,,' \
  --transform 's/zanata-.*\.war/zanata.war/' |
    docker build -t zanata-dev --file Dockerfile.zanata -

docker-compose up
