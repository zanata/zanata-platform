#!/bin/bash -eu
### This program sets up the data volume and database containers for Zanata

# determine directory containing this script: https://stackoverflow.com/a/246128/14379
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"

source ${DIR}/common

VOLUME_DIR=$HOME/docker-volumes/zanata-mariadb

mkdir -p $VOLUME_DIR

CONTAINER_NAME=zanatadb
VOLUME_OPT="-v $VOLUME_DIR:/var/lib/mysql:Z"
EPHEMERAL=false
while getopts ":n:eh" opt; do
  case ${opt} in
    n)
      echo "===== set docker network to $OPTARG ====="
      DOCKER_NETWORK="$OPTARG"
      ;;
    h)
      echo "========   HELP   ========="
      echo "-n <docker network>: will connect container to given docker network (default is $DOCKER_NETWORK)"
      echo "-h                 : display help"
      exit
      ;;
    e)
      echo "===== starting an ephemeral database container ===="
      if [ ! -z  $(docker ps --all --quiet --filter name=$CONTAINER_NAME) ]; then
           echo "removing $CONTAINER_NAME container"
           docker rm -f $CONTAINER_NAME
      fi
      # instead of giving the container volume mapping,
      # we tell it to remove itself once stopped.
      VOLUME_OPT=" --rm "
      EPHEMERAL=true
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -h for help" >&2
      exit 1
      ;;
  esac
done

ensure_docker_network

docker run --name $CONTAINER_NAME \
  -e MYSQL_USER=$DB_USERNAME -e MYSQL_PASSWORD=$DB_PASSWORD \
  -e MYSQL_DATABASE=$DB_SCHEMA -e MYSQL_ROOT_PASSWORD=$DB_ROOT_PASSWORD \
  -P --net=${DOCKER_NETWORK} \
  ${VOLUME_OPT} \
  -d mariadb:10.1 \
  --character-set-server=utf8 --collation-server=utf8_general_ci

echo ''
echo "Please use the command 'docker logs $CONTAINER_NAME' to check that MariaDB starts correctly."

if [ "$EPHEMERAL" == 'true' ]
then
    echo "====================================================================="
    echo "Once MariaDB and Zanata are running, execute below commands to create an admin user"
    echo "docker cp $DIR/conf/admin-user-setup.sql $CONTAINER_NAME:/tmp/"
    echo "docker exec $CONTAINER_NAME /bin/sh -c 'mysql -u$DB_USERNAME -p$DB_PASSWORD $DB_SCHEMA </tmp/admin-user-setup.sql'"
    echo "====================================================================="
fi
