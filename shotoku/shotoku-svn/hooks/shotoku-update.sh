#!/bin/bash

# TRIGGERS A SHOTOKU WC UPDATE ON A GIVEN REMOTE MACHINE

# CONFIGURATION
# Username&password to access the servlet
USERNAME="ShotokuUpdate"
PASSWORD="ShotokuUpdate"
# Address of the shotoku update servlet
REMOTE="http://192.168.1.102:8080/shotoku-admin/update"
# Shotoku id of the repository (as in shotoku.properties)
ID=priv
# Prefix of the repository (as in shotoku.properties, relative to the root
# of the whole svn repository, without a slash in the beginning, with a 
# trailing slash.
REPO_PREFIX="private/adamw/test/"
# -------------

REPOS="$1"
REV="$2"
OPTS="--revision $2"

SVNLOOK=svnlook

PREFIX_LEN=`echo "$REPO_PREFIX" | wc -m`
SVNLOOK_DATA=`$SVNLOOK dirs-changed $OPTS $REPOS | grep "^$REPO_PREFIX" | cut -b $PREFIX_LEN- | sort | uniq | tr [:space:] :`

if [ -n "$SVNLOOK_DATA" ]
then
	POST_DATA="data=$ID#$REV#$SVNLOOK_DATA"
	
	wget -O - -o - --http-user=$USERNAME --http-password=$PASSWORD --post-data="$POST_DATA" $REMOTE >/dev/null
fi

exit 0
