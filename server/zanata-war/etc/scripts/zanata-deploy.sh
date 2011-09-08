#!/bin/bash -e

# Script: zanata-deploy.sh
# Author: sflaniga@redhat.com

# This script deploys zanata.war to a target machine,
# based on a Jenkins job name 
# and some configuration variables read from ~/.config/zanata-deploy.conf.

# Requirements:

# 1. env var WARNING_EMAIL must be set via zanata-deploy.conf
# 2. Jenkins vars BUILD_TAG, JOB_NAME, GIT_BRANCH set by Jenkins
# NB Jenkins will only set $GIT_BRANCH if the build config specifies a single branch
# 3. Jenkins job name should look like zanata-VER-PROFILE or zanata-build-deploy-VER-PROFILE
# 4. minimal zanata-deploy.conf should look something like this:
# master_version=1.5
# host_1_4_jaas=jaastest.example.com
# host_1_4_internal=internaltest.example.com
# NB replace . with _ in variable names

# Each target box should be set up in a certain way.  
# See the default values for url, user, service and targetfile near the
# end of this script, or override them for each affected host, eg:
# host_1_5_special=specialcase.example.com
# url_1_5_special=https://specialcase.example.com/
# user_1_5_special=jenkins
# service_1_5_special="sudo /etc/init.d/jbossas"
# targetfile_1_5_special="/opt/jboss-as/server/default/deploy/ROOT.war"


if [ -L $0 ] ; then
    ME=$(readlink $0)
else
    ME=$0
fi
DIR=$(dirname $ME)

source $HOME/.config/zanata-deploy.conf

BUILD_TAG=${BUILD_TAG-<unknown build>}
JOB_NAME=${JOB_NAME-<unknown job>}
WARNING_EMAIL=${WARNING_EMAIL-test@example.com}
ssh=${ssh-ssh}
scp=${scp-scp}
mail=${mail-mail}

# functions:

die() {
   echo "zanata-deploy: $1" >&2
   echo "zanata-deploy: $1" | $mail -s "zanata-deploy error" $WARNING_EMAIL
   exit 0
}

arrayGet() { 
    local array=$1 index=$2
    local i="${array}_${index}"
    echo ${!i}
}


# main:

warfile=server/zanata-war/target/*.war

echo "BUILD_TAG: $BUILD_TAG"
echo "GIT_BRANCH: $GIT_BRANCH"

if [[ $JOB_NAME =~ zanata-(build-deploy-)?(([^-][^-]*)-)?(.*) ]]; then
   #branch_name=${BASH_REMATCH[3]}
   auth=${BASH_REMATCH[4]}
else
   die "can't find type of build for job name $JOB_NAME, for $BUILD_TAG"
fi

branch_name=$GIT_BRANCH

if [[ "$branch_name" == "" ]]; then
  die "can't determine branch name for $BUILD_TAG"
fi

if [[ "$branch_name" == "master" ]]; then
   version=$master_version
else
   version=$branch_name
fi

echo branch: $branch_name
echo version: $version
echo auth: $auth

# replace . with _ in version:
ver=${version//./_}

host=$(arrayGet host ${ver}_${auth})
if [[ -z $host ]]; then
   die "no host configured for ver $ver, auth $auth, build $BUILD_TAG"
fi

url=$(arrayGet url ${ver}_${auth})
if [[ -z $url ]]; then
   url=http://$host:8080/
fi

user=$(arrayGet user ${ver}_${auth})
if [[ -z $user ]]; then
   user=jboss
fi

service=$(arrayGet service ${ver}_${auth})
if [[ -z $service ]]; then
   service="JBOSS_USER=RUNASIS /etc/init.d/jbossewp5"
fi

targetfile=$(arrayGet targetfile ${ver}_${auth})
if [[ -z $targetfile ]]; then
   targetfile=/opt/jboss-ewp-5.0/jboss-as-web/server/production/deploy/ROOT.war
fi

echo host: $host
echo url: $url
echo user: $user
echo service: $service
echo targetfile: $targetfile
#set -x
$ssh $user@$host $service stop
$scp $warfile $user@$host:$targetfile
$ssh $user@$host $service start
#set +x
echo $url is now starting up

$DIR/is_server_up.sh $url
