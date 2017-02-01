#!/bin/bash

# determine directory containing this script
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

source ${DIR}/common

VOLUME_DIR=$HOME/docker-volumes/zanata-mariadb

mkdir -p $VOLUME_DIR
sudo chcon -Rt svirt_sandbox_file_t $VOLUME_DIR

while getopts ":n:h" opt; do
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
    \?)
      echo "Invalid option: -${OPTARG}. Use -h for help" >&2
      exit 1
      ;;
  esac
done

ensure_docker_network

docker run --name zanatadb \
  -e MYSQL_USER=$DB_USERNAME -e MYSQL_PASSWORD=$DB_PASSWORD \
  -e MYSQL_DATABASE=$DB_SCHEMA -e MYSQL_ROOT_PASSWORD=$DB_ROOT_PASSWORD \
  -P --net=${DOCKER_NETWORK} \
  -v $VOLUME_DIR:/var/lib/mysql \
  -d mariadb:10.1 \
  --character-set-server=utf8 --collation-server=utf8_general_ci

echo ''
echo 'Please use the command "docker logs zanatadb" to check that MariaDB starts correctly.'
