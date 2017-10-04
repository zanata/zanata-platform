#!/bin/bash

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
