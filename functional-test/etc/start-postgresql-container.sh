#!/bin/bash

# =============================================
# Parameters:
# 1. Container name (to shut it down later)
# 2. Postgresql port
# =============================================

DOCKER_CMD="sudo docker run -e POSTGRES_USER=zanata -e POSTGRES_PASSWORD=zanata -p $2:5432 -d --name $1 postgres:9.2"
POSTGRESQL_CONTAINER_ID=$($DOCKER_CMD)
echo "Started Postgresql container $POSTGRESQL_CONTAINER_ID"
echo "with line '$DOCKER_CMD'"

# PGQL_PORT=$(docker inspect -f '{{ (index (index .NetworkSettings.Ports "5432/tcp") 0).HostPort }}' $1)
# echo "Postgresql port: $PGQL_PORT"
