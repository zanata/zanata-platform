#!/bin/bash

docker build -t wildfly-base .

# First transform flattens everything
# Second transform removes the version in zanata.war
# Third transform standardizes the Dockerfile name
tar --create \
  Dockerfile.wildfly \
  ../zanata-war/target/zanata-*.war \
  --transform 's,.*/,,' \
  --transform 's/zanata-.*\.war/zanata.war/' \
  --transform 's/Dockerfile.wildfly/Dockerfile/' |
docker build -t zanata-dev -

docker-compose up
