#!/bin/sh
SELENIUM_SERVER_JAR=
SELENIUM_SERVER_CMD=`which selenium-server`
SELENIUM_SERVER_ARGS=
RESULT_DIR=results

# base URL for test to be run at:
BASE_URL="http://hudson.englab.bne.redhat.com/"


if [ -z ${SELENIUM_SERVER_CMD} ]; then
	SELENIUM_SERVER_JAR = find $HOME/.m2/repository/org/seleniumhq/selenium/server/selenium-server/ -name "selenium-server*.jar" | head --lines=1
	if [ -z ${SELENIUM_SERVER_JAR} ]; then
		echo "No selenium server founded, please install it"
		exit 1
        fi
	SELENIUM_SERVER_CMD=java -jar ${SELENIUM_SERVER_JAR}
fi

#echo "SELENIUM_SERVER_CMD=${SELENIUM_SERVER_CMD}"
cd resources
for suiteFile in `find . -name '0-*.html'`; do
    for browser in "*firefox"; do
        resultFile_name=`echo -n ${suiteFile}|sed "s+.*0-++" | sed "s+.html$+.result.html+"`
        resultFile="../${RESULT_DIR}/${resultFile_name}"
        CMD="${SELENIUM_SERVER_CMD} ${SELENIUM_SERVER_ARGS} -htmlsuite ${browser} ${BASE_URL} ${suiteFile} ${resultFile}"
        echo "$CMD"
        eval "$CMD";
    done
done

