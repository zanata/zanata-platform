#!/bin/bash -ex


git fetch
git checkout integration/master && git reset --hard origin/integration/master
git branch -D legacy release || true

git checkout legacy
git merge origin/release --ff-only --quiet ||
(echo please check for cherry-picked commits in legacy which were never merged into release; exit 1)

git checkout release
git merge origin/master --ff-only --quiet ||
(echo please check for cherry-picked commits in release which were never merged into master; exit 1)

git checkout integration/master
mvn release:update-versions -DautoVersionSubmodules=true # -DdevelopmentVersion=${developmentVersion}
git commit pom.xml */pom.xml -m "prepare for next development iteration"

# push all the changes back to the server
git push origin legacy release integration/master

