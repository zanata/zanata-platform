#!/bin/sh
# Usage: $0 <testRole> <testSuitePath> <testSuiteName> <siOut> <siSoOut>

testRole=$1
testSuitePath=$2
testSuiteName=$3
siOut=$4
siSoOut=$5
testRoot=$6

SI_FILE_NORMAL="SignInNormal.html"
SI_FILE_ADMIN="SignInAdmin.html"
SI_PATTERN_MATCH="Test Suite</b></td></tr>"
case $testRole in 
        'ADMIN' )
            SI_FILE=${SI_FILE_ADMIN}
            ;;
        * )
            SI_FILE=${SI_FILE_NORMAL}
            ;;
esac
SI_PATTERN_REPLACE="${SI_PATTERN_MATCH}\n<tr><td><a href=\"${SI_FILE}\">${testRole} Sign In</a></td></tr>"
ln -sf ${testRoot}/${SI_FILE} ${testSuitePath}

SO_FILE="SignOut.html"
SO_PATTERN_MATCH="</tbody>"
SO_PATTERN_REPLACE="<tr><td><a href=\"${SO_FILE}\">Sign Out</a></td></tr>\n${SO_PATTERN_MATCH}"
ln -sf ${testRoot}/${SO_FILE} ${testSuitePath}

### Write Selenium test files
cat ${testSuitePath}/0-${testSuiteName}.html | sed -e "s|${SI_PATTERN_MATCH}|${SI_PATTERN_REPLACE}|" > ${testSuitePath}/${siOut}
cat ${testSuitePath}/${siOut} | sed -e "s|${SO_PATTERN_MATCH}|${SO_PATTERN_REPLACE}|" > ${testSuitePath}/${siSoOut}

