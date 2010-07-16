#!/bin/bash

EXTRA_DIR=target/extra/webtrans/
TARGET_DIR=src/main/resources/

SUFFIX='_default.properties'
for i in `ls $EXTRA_DIR*$SUFFIX`; 
do
  # remove EXTRA_DIR prefix
  NEW_FILE=${i#$EXTRA_DIR}

  # remove SUFFIX
  NEW_FILE=${NEW_FILE%$SUFFIX}

  NEW_FILE=$TARGET_DIR${NEW_FILE//./\/}.properties
  echo "syncing $NEW_FILE";
  cp $i $NEW_FILE;
done


