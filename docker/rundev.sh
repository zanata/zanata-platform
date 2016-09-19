#!/bin/bash -x

# determine directory containing this script
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# change to top of the git working directory
cd $DIR/../
ZANATA_WAR=$(echo $PWD/zanata-war/target/zanata-*.war)

if [ -f "$ZANATA_WAR" ]
then
    chcon -Rt svirt_sandbox_file_t "$ZANATA_WAR"
else
    echo "===== NO war file found. Please build Zanata war first ====="
    exit 1
fi

# volume mapping for zanata server files
ZANATA_DIR=$HOME/docker-volumes/zanata
# create the data directory and set permissions (SELinux)
mkdir -p $ZANATA_DIR && chcon -Rt svirt_sandbox_file_t "$ZANATA_DIR"
# make zanata directory and standalone.xml file accessible to docker containers (SELinux)

# build the docker dev image
docker build -t zanata/server-dev docker/

# OutOfMemoryError handling:
#  The heap will be dumped to a file on the host, eg ~/docker-volumes/zanata/java_pid63.hprof
#  By default, we will keep the JVM running, so that a debugger can be attached.
#  Alternative option: -XX:OnOutOfMemoryError='kill -9 %p'

# runs zanata/server-dev:latest docker image
docker run \
    -e JAVA_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/jboss/zanata" \
    --rm --name zanata --link zanatadb:db \
    -p 8080:8080 -p 8787:8787 -it \
    -v $ZANATA_WAR:/opt/jboss/wildfly/standalone/deployments/ROOT.war \
    -v $ZANATA_DIR:/opt/jboss/zanata \
    zanata/server-dev
