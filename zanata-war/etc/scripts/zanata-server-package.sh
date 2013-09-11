#!/bin/bash -e

# Script: zanata-server-package.sh
# Author: camunoz@redhat.com

# This script prepares a Zanata standalone server package
# based on JBoss 7.2.0 AS Community version.
#
# Parameters:
# 1. JBoss 7 zip distribution location.
# 2. Zanata war location.
#
# Pre-requisites:
# 1. Apache maven in the path

# Mysql Driver version to be used
MYSQL_DRV_VERSION=5.1.18

# Get the JBoss 7 zip location from parameters
JBOSS_ZIP_LOC=$1
JBOSS_ZIP_PARENT=${JBOSS_ZIP_LOC%/*}

# Get the Zanata war location from parameters
ZANATA_WAR=$2


# Zanata War project Dir
ZANATA_WAR_HOME=../..

# Temporary directory to extract and manipulate files
TMP_DIR=${JBOSS_ZIP_PARENT}/jbosstmp

# Ask for parameters
echo -n "Version number to be built? "
read zanata_version

# Extract the JBoss package
echo 'Extracting JBoss package...'
unzip -q $JBOSS_ZIP_LOC -d $TMP_DIR

JBOSS_TMP_DIR=$TMP_DIR/$(ls $TMP_DIR | sort -n | head -1)

# Remove unnecessary files from the package
echo 'Customizing JBoss release...'
rm -rf $JBOSS_TMP_DIR/bin/client
rm -rf $JBOSS_TMP_DIR/docs

# Add Zanata specific files
cp $ZANATA_WAR $JBOSS_TMP_DIR/standalone/deployments/zanata.war
cp $ZANATA_WAR_HOME/src/etc/zanata-ds.xml $JBOSS_TMP_DIR/standalone/deployments
patch -p1 --verbose -d $JBOSS_TMP_DIR/ < $ZANATA_WAR_HOME/etc/public-package/patches/as7.patch

# Get Maven dependencies
mvn dependency:get -DrepoUrl=http://repo1.maven.org -Dartifact=mysql:mysql-connector-java:$MYSQL_DRV_VERSION
cp ~/.m2/repository/mysql/mysql-connector-java/$MYSQL_DRV_VERSION/mysql-connector-java-$MYSQL_DRV_VERSION.jar $JBOSS_TMP_DIR/standalone/deployments/mysql-connector-java.jar

# Create zanata start scripts

echo "# Zanata start script" >> $JBOSS_TMP_DIR/bin/start-zanata.sh
echo "./standalone.sh" >> $JBOSS_TMP_DIR/bin/start-zanata.sh
echo "standalone.bat" >> $JBOSS_TMP_DIR/bin/start-zanata.bat
chmod a+x $JBOSS_TMP_DIR/bin/start-zanata.sh

# Rename the JBoss temporary directory
mv $JBOSS_TMP_DIR $TMP_DIR/zanata-server-$zanata_version

# Pack the distribution
echo 'Building distributable archive...'
cd $TMP_DIR
zip -rq zanata-server-${zanata_version}.zip zanata-server-$zanata_version
mv zanata-server-${zanata_version}.zip ..

# Remove temp dir
rm -rf $TMP_DIR
