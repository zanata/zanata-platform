#!/bin/bash

# Change these for different settings
DB_USERNAME=zanata
DB_PASSWORD=zanatapw
DB_SCHEMA=zanata
DB_ROOT_PASSWORD=rootpw
# default docker network to join
DOCKER_NETWORK=docker-network

VOLUME_DIR=$HOME/docker-volumes/zanata-mariadb

mkdir -p $VOLUME_DIR
chcon -Rt svirt_sandbox_file_t $VOLUME_DIR

# docker network option

while getopts ":n:H" opt; do
  case ${opt} in
    n)
      echo "===== set docker network to $OPTARG ====="
      DOCKER_NETWORK="$OPTARG"
      ;;
    H)
      echo "========   HELP   ========="
      echo "-n <docker network>: will connect container to given docker network (default is $DOCKER_NETWORK)"
      echo "-H                 : display help"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -H for help" >&2
      exit 1
      ;;
  esac
done

# check if the docker network is already created
if docker network ls | grep -w ${DOCKER_NETWORK}
then
    echo "will use docker network $DOCKER_NETWORK"
else
    echo "creating docker network $DOCKER_NETWORK"
    docker network create ${DOCKER_NETWORK}
fi

docker run --name zanatadb \
  -e MYSQL_USER=$DB_USERNAME -e MYSQL_PASSWORD=$DB_PASSWORD \
  -e MYSQL_DATABASE=$DB_SCHEMA -e MYSQL_ROOT_PASSWORD=$DB_ROOT_PASSWORD \
  -P --net=${DOCKER_NETWORK} \
  -v $VOLUME_DIR:/var/lib/mysql \
  -d mariadb:10.1 \
  --character-set-server=utf8 --collation-server=utf8_general_ci

echo ''
echo 'Please use the command "docker logs zanatadb" to check that MariaDB starts correctly.'
