#!/bin/bash

EXTRA_DIR=target/extra/webtrans/
TARGET_DIR=src/main/resources/

SUFFIX='_en.properties'

echo "Processing Messages";

for i in `ls $EXTRA_DIR*$SUFFIX`; 
do
  # remove EXTRA_DIR prefix
  NEW_FILE=${i#$EXTRA_DIR}

  # remove SUFFIX
  NEW_FILE=${NEW_FILE%$SUFFIX}

  NEW_FILE=$TARGET_DIR${NEW_FILE//./\/}.properties
  echo "syncing $NEW_FILE";
  mkdir -p `dirname $NEW_FILE`;
  cp $i $NEW_FILE;
done

echo "Processing UiBinder Messages";

SUFFIX='.properties'

for i in `ls $EXTRA_DIR*UiBinderImplGenMessages$SUFFIX`; 
do
  # remove EXTRA_DIR prefix
  NEW_FILE=${i#$EXTRA_DIR}

  # remove SUFFIX
  NEW_FILE=${NEW_FILE%$SUFFIX}

  NEW_FILE=$TARGET_DIR${NEW_FILE//./\/}.properties
  echo "syncing $NEW_FILE";
  mkdir -p `dirname $NEW_FILE`;
  cp $i $NEW_FILE;
done




