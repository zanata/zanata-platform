# Contributing to Zanata

Pull requests welcome!

General information about our Maven setup is in the wiki:
https://github.com/zanata/zanata-server/wiki/Developer-Guide

Our CI server merges changes from `integration/master` to `master` after testing, so: **please branch from `master`, but please target `integration/master` in pull requests.**

To run all tests against WildFly (takes about 1 hour):

    mvn clean verify -Dappserver=wildfly8 -DstaticAnalysis -Dchromefirefox


This guide is very much a work in progress, so please ask questions if anything is unclear or missing: https://www.redhat.com/mailman/listinfo/zanata-devel
