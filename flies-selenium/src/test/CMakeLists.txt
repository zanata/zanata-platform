cmake_minimum_required(VERSION 2.6)
####################################################################
# Init Definition
####################################################################
SET(CMAKE_ALLOW_LOOSE_LOOP_CONSTRUCTS ON)

####################################################################
# Project basic information
####################################################################
PROJECT(flies)
SET(PROJECT_DESCRIPTION "Online translator collaboration system")
# base URL for test to be run at:
SET(BASE_URL "http://hudson.englab.bne.redhat.com/")
SET(SELENIUM_SERVER_ARGS )
SET(RESULT_DIR results)
SET(MAVEN_REPOSITORY "$ENV{HOME}/.m2/repository/")
SET(MAVEN_SELENIUM_SERVER_PATH "${MAVEN_REPOSITORY}/org/seleniumhq/selenium/server/selenium-server/")
SET(SELENIUM_SEARCH_PATHS ${MAVEN_SELENIUM_SERVER_PATH} /usr/share/java)
SET(BROWSERS_TO_TEST firefox googlechrome)

SET(firefox_SEARCH_PATHS /usr/lib64/firefox-3.6 /usr/lib/firefox-3.6 /usr/lib64/firefox-3.5 /usr/lib/firefox-3.5
    /usr/lib64/firefox-3.4 /usr/lib/firefox-3.4 /usr/lib64/firefox-3 /usr/lib/firefox-3
    /usr/lib64/firefox /usr/lib/firefox)

SET(opera_SEARCH_PATHS /usr/lib64/opera /usr/lib/opera /opt/opera)

SET(googlechrome_SEARCH_PATHS /opt/google/chrome)

MACRO(FIND_FILE_IN_DIRS var pattern searchPaths)
    FOREACH(fileDir ${searchPaths})
        IF (EXISTS ${fileDir})
            MESSAGE("Finding ${pattern} in ${fileDir}")
            EXECUTE_PROCESS(COMMAND find "${fileDir}" -name "${pattern}" -type f
                COMMAND head --lines=1
                OUTPUT_VARIABLE fileObj_raw
            )
            IF ( fileObj_raw )
                MESSAGE(" Found!")
                BREAK()
            ELSE()
                MESSAGE(" Not Found!")
            ENDIF()
        ELSE()
            MESSAGE("Directory ${fileDir} does not exist, skip.")
        ENDIF()
    ENDFOREACH() 
    IF ( fileObj_raw )
        STRING(STRIP ${fileObj_raw} fileObj)
        SET(${var} ${fileObj})
    ELSE()
        SET(${var} "${var}-NOTFOUND")
    ENDIF()
ENDMACRO()

FIND_PROGRAM(SELENIUM_SERVER_CMD selenium-server)
IF( ${SELENIUM_SERVER_CMD} STREQUAL "SELENIUM_SERVER_CMD-NOTFOUND" )
    # find selenium server jar
    FIND_FILE_IN_DIRS(SELENIUM_SERVER_JAR "selenium-server*.jar" "${SELENIUM_SEARCH_PATHS}")    
    IF ( ${SELENIUM_SERVER_JAR} STREQUAL "SELENIUM_SERVER_JAR-NOTFOUND" )
        MESSAGE(FATAL_ERROR "selenium-server not found, install it please.")
    ENDIF()
    SET(SELENIUM_SERVER_CMD java -jar ${SELENIUM_SERVER_JAR})
ENDIF()
MESSAGE("SELENIUM_SERVER_CMD=|${SELENIUM_SERVER_CMD}|")



### Find the browser binary
FIND_FILE_IN_DIRS(firefox_BIN firefox "${firefox_SEARCH_PATHS}")
MESSAGE("firefox_BIN=${firefox_BIN}")
 
#FIND_FILE_IN_DIRS(googlechrome_BIN chrome "${googlechrome_SEARCH_PATHS}")
MESSAGE("googlechrome_BIN=${googlechrome_BIN}")

FIND_FILE_IN_DIRS(opera_BIN opera "${opera_SEARCH_PATHS}")
MESSAGE("opera_BIN=${opera_BIN}")


####################################################################
# Sub directories
####################################################################
# ADD_SUBDIRECTORY(resources)

####################################################################
# Test Suites.
####################################################################
ENABLE_TESTING()

FOREACH(browser ${BROWSERS_TO_TEST} )
    FILE(GLOB_RECURSE TEST_SUITES  RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}/resources "0-*.html")
    MESSAGE("TEST_SUITES=${TEST_SUITES}")
    IF (${browser} STREQUAL "firefox")
         SET(BROWSER_STR "*${browser} ${${browser}_BIN}")
    ELSE()
         SET(BROWSER_STR "*${browser}")
    ENDIF()
    FOREACH(testSuite ${TEST_SUITES})
        GET_FILENAME_COMPONENT(testSuitePath ${testSuite} PATH)
        GET_FILENAME_COMPONENT(testSuiteNameOrig ${testSuite} NAME_WE)
        STRING(REGEX REPLACE "^[0-9]-" "" testSuiteName ${testSuiteNameOrig})
        ADD_TEST("${testSuiteName}_on_${browser}" ${SELENIUM_SERVER_CMD} ${SELENIUM_SERVER_ARG} 
             -htmlsuite "${BROWSER_STR}" "${BASE_URL}"
             ${CMAKE_CURRENT_SOURCE_DIR}/resources/${testSuitePath}/${testSuiteNameOrig}.html
             ${CMAKE_CURRENT_SOURCE_DIR}/${RESULT_DIR}/${testSuiteName}_on_${browser}.result.html )
   ENDFOREACH()
ENDFOREACH()
#        ADD_TEST("TranslationEditor_Editing" selenium-server
#             -htmlsuite '*firefox' '${BASE_URL}' resources/TranslationEditor/Editing/0-TranslationEditor_Editing.html
#             results/TranslationEditor_Editing.result.html )


