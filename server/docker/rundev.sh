#!/usr/bin/env bash
set -eu

# determine directory containing this script: https://stackoverflow.com/a/246128/14379
SOURCE="${BASH_SOURCE[0]}"
declare SCRIPT_DIR
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$SCRIPT_DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null && pwd )"

source $SCRIPT_DIR/common

# change to the server directory
cd $SCRIPT_DIR/../
# JBoss ports
HTTP_PORT=8080
DEBUG_PORT=8787
MGMT_PORT=9090

# default mail setting
MAIL_HOST=localhost
MAIL_CREDENTIAL_ENV=''

while getopts ":e:p:n:hl:" opt; do
  case ${opt} in
    e)
      echo "==== use $OPTARG as mail host ===="
      MAIL_HOST=$OPTARG
      ;;
    l)
      echo "==== provide smtp host login (username:password) ===="
      # set the internal field separator (IFS) variable, and then let it parse into an array.
      # When this happens in a command, then the assignment to IFS only takes place to that single command's environment (to read ).
      # It then parses the input according to the IFS variable value into an array
      IFS=':' read -ra CREDENTIAL <<< "$OPTARG"
      if [ ${#CREDENTIAL[@]} -ne 2 ]
      then
        echo "must provide smtp credentials in username:password format (colon separated)"
        exit 1
      fi
      MAIL_USERNAME=${CREDENTIAL[0]}
      MAIL_PASSWORD=${CREDENTIAL[1]}
      MAIL_CREDENTIAL_ENV=" -e MAIL_USERNAME=\"${MAIL_USERNAME}\" -e MAIL_PASSWORD=\"${MAIL_PASSWORD}\" "
      ;;
    p)
      echo "===== set JBoss port offset to $OPTARG ====="
      if [ "$OPTARG" -eq "$OPTARG" ] 2>/dev/null
      then
        HTTP_PORT=$(($OPTARG + 8080))
        DEBUG_PORT=$(($OPTARG + 8787))
        MGMT_PORT=$(($OPTARG + 9090))
        echo "===== http port       : $HTTP_PORT"
        echo "===== debug port      : $DEBUG_PORT"
        echo "===== management port : $MGMT_PORT"
      else
        echo "===== MUST provide an integer as argument ====="
        exit 1
      fi
      ;;
    n)
      echo "===== set docker network to $OPTARG ====="
      DOCKER_NETWORK=$OPTARG
      ;;
    h)
      set +x
      echo "========   HELP   ========="
      echo "-e <smtp email host>  : smtp mail host"
      echo "-l <username:password>: smtp login: username and password separated by colon"
      echo "-p <offset number>    : set JBoss port offset"
      echo "-n <docker network>   : will connect container to given docker network (default is $DOCKER_NETWORK)"
      echo "-h                    : display help"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -h for help" >&2
      exit 1
      ;;
  esac
done

ZANATA_WAR=$(echo $PWD/zanata-war/target/zanata-*.war)

# volume mapping for JBoss deployment folder (put exploded war or war file here to deploy)
ZANATA_DEPLOYMENTS_DIR=$HOME/docker-volumes/zanata-deployments
# make zanata deployment directory accessible to docker containers (SELinux)
mkdir -p ${ZANATA_DEPLOYMENTS_DIR}

if [ -f "$ZANATA_WAR" ]
then
    # remove old file (hardlink) first
    rm -f ${ZANATA_DEPLOYMENTS_DIR}/ROOT.war
    # we can not use symlink as JBoss inside docker can't properly read the symlink file
    # try to link or copy the war file to deployments directory
    ln ${ZANATA_WAR} ${ZANATA_DEPLOYMENTS_DIR}/ROOT.war || cp ${ZANATA_WAR} ${ZANATA_DEPLOYMENTS_DIR}/ROOT.war
else
    echo "===== NO war file found (or more than one). Please build Zanata war first (or delete old versions) ====="
    exit 1
fi

set -x

ensure_docker_network

# volume mapping for zanata server files
ZANATA_DIR=$HOME/docker-volumes/zanata
# create the data directory and set permissions (SELinux)
mkdir -p $ZANATA_DIR

# make config scripts available inside Docker
mkdir -p docker/target
cp -a etc/scripts/jboss-cli-jjs etc/scripts/configure-app-server.js docker/target/

# build the docker dev image
# TODO rename docker/Dockerfile to docker/Dockerfile.zanata-base
docker build --tag zanata/server-dev --file docker/Dockerfile docker/

# OutOfMemoryError handling:
#  The heap will be dumped to a file on the host, eg ~/docker-volumes/zanata/java_pid63.hprof
#  By default, we will keep the JVM running, so that a debugger can be attached.
#  Alternative option: -XX:OnOutOfMemoryError='kill -9 %p'

JBOSS_DEPLOYMENT_VOLUME=/opt/jboss/wildfly/standalone/deployments/

# TODO run docker-compose up, passing in user options. see rundev-compose.sh

# runs zanata/server-dev:latest docker image
docker run \
    -e PREPEND_JAVA_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/jboss/zanata" \
    -e DB_USERNAME=${DB_USERNAME} -e DB_PASSWORD=${DB_PASSWORD} -e DB_SCHEMA=${DB_SCHEMA} -e DB_HOSTNAME=zanatadb\
    -e MAIL_HOST="${MAIL_HOST}" ${MAIL_CREDENTIAL_ENV} \
    --rm --name zanata --net=${DOCKER_NETWORK} \
    -p ${HTTP_PORT}:8080 -p ${DEBUG_PORT}:8787 -p ${MGMT_PORT}:9990 -it \
    -v ${ZANATA_DEPLOYMENTS_DIR}:${JBOSS_DEPLOYMENT_VOLUME}:Z \
    -v $ZANATA_DIR:/opt/jboss/zanata:Z \
    zanata/server-dev
