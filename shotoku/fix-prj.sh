#!/bin/bash
FILES=`find . -path "*.svn" -prune -o -print | grep project.properties`
NEW_LINE="maven.repo.remote=http:\/\/repo1.maven.org\/maven"
OLD_LINE="maven.repo.remote=.*"

for FILE in $FILES
do
	sed "s/$OLD_LINE/$NEW_LINE/" < $FILE > $FILE.out
	mv $FILE.out $FILE
done
