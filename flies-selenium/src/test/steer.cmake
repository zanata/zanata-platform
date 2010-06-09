cmake_minimum_required(VERSION 2.4)
####################################################################
# Init Definition
####################################################################
SET(CMAKE_ALLOW_LOOSE_LOOP_CONSTRUCTS ON)
MESSAGE("CMake version=${CMAKE_VERSION}")

IF(CMAKE_VERSION)
    IF(CMAKE_VERSION VERSION_LESS 2.6)
        MESSAGE("SET CMAKE_BACKWARDS_COMPATIBILITY ${CMAKE_VERSION}")
        SET(CMAKE_BACKWARDS_COMPATIBILITY ${CMAKE_VERSION})
    ENDIF()
ELSE()
    # CMAKE_VERSION may not available in 2.4
    MESSAGE("SET CMAKE_BACKWARDS_COMPATIBILITY ${CMAKE_VERSION}")
    SET(CMAKE_BACKWARDS_COMPATIBILITY 2.4)
ENDIF()

SET(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_SOURCE_DIR})
SET(ENV{LC_ALL} "C")
INCLUDE(BasicMacros.cmake)

EXECUTE_PROCESS(COMMAND pwd
    OUTPUT_VARIABLE PWD)
STRING_TRIM(PWD ${PWD})
SET(CTEST_SOURCE_DIRECTORY  "${PWD}")
SET(CTEST_BINARY_DIRECTORY  "${CTEST_SOURCE_DIRECTORY}")
SET(TEST_CFG "${CTEST_BINARY_DIRECTORY}/test.cfg")
MESSAGE("TEST_CFG=${TEST_CFG}")

####################################################################
# Project basic information (Place to customize)
####################################################################
#PROJECT(flies-selenium)
SET(PROJECT_DESCRIPTION "Online translator collaboration system")
# base URL for test to be run at:
SETTING_FILE_GET_ATTRIBUTE(BASE_URL "BASE_URL" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(FLIES_URL "FLIES_URL" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(REST_PATH "REST_PATH" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(SELENIUM_SERVER_ARGS "SELENIUM_SERVER_ARGS" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(RESULT_DIR "RESULT_DIR" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(BROWSERS_TO_TEST "BROWSERS_TO_TEST" "${TEST_CFG}" UNQUOTED)
STRING(REGEX MATCHALL "[^ \t]+" BROWSERS_TO_TEST "${BROWSERS_TO_TEST}")
MESSAGE("BROWSERS_TO_TEST=${BROWSERS_TO_TEST}")

# For hudson testing/reporting
SETTING_FILE_GET_ATTRIBUTE(HUDSON_HOME "HUDSON_HOME" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(FLIES_URL "FLIES_URL" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(TEST_LOGFILE "TEST_LOGFILE" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(HEADLESS_DISPLAY "HEADLESS_DISPLAY" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(JOB_NAME "JOB_NAME" "${TEST_CFG}" UNQUOTED)
CONFIGURE_FILE(${CTEST_SOURCE_DIRECTORY}/hudson_test_wrap.sh.in ${CTEST_BINARY_DIRECTORY}/hudson_test_wrap.sh)

#===================================================================
# Flies Projects to be test
SETTING_FILE_GET_ATTRIBUTE(SAMPLE_PROJ_DIR	"SAMPLE_PROJ_DIR" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(LANGS	"LANGS" "${TEST_CFG}" UNQUOTED)

SETTING_FILE_GET_ATTRIBUTE(INIT_ITER	"INIT_ITER" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(INIT_ITER_NAME	"INIT_ITER_NAME" "${TEST_CFG}" UNQUOTED)
SETTING_FILE_GET_ATTRIBUTE(INIT_ITER_DESC	"INIT_ITER_DESC" "${TEST_CFG}" UNQUOTED)

SETTING_FILE_GET_ATTRIBUTE(PUBLICAN_PROJECTS	"PUBLICAN_PROJECTS" "${TEST_CFG}" UNQUOTED)

#===================================================================
# Search Paths
SET(MAVEN_REPOSITORY "$ENV{HOME}/.m2/repository/")
SET(MAVEN_SELENIUM_SERVER_PATH "${MAVEN_REPOSITORY}/org/seleniumhq/selenium/server/selenium-server/")
SET(SELENIUM_SEARCH_PATHS $ENV{HOME} ${MAVEN_SELENIUM_SERVER_PATH} /usr/share/java)

SET(firefox_SEARCH_PATHS /usr/lib64/firefox-3.6 /usr/lib/firefox-3.6 /usr/lib64/firefox-3.5 /usr/lib/firefox-3.5
    /usr/lib64/firefox-3* /usr/lib/firefox-3* /usr/lib64/firefox* /usr/lib/firefox*)
SET(firefox_BIN_NAME firefox)

SET(opera_SEARCH_PATHS /usr/lib64/opera /usr/lib/opera /opt/opera)
SET(opera_BIN_NAME opera)

SET(googlechrome_SEARCH_PATHS /opt/google/chrome)
SET(googlechrome_BIN_NAME google-chrome)

#===================================================================
# Macro FIND_FILE_IN_DIRS
MACRO(FIND_FILE_IN_DIRS var pattern searchPaths)
    FOREACH(sPath ${searchPaths})
	SET (fileObj_raw  )
	FILE(GLOB sDirs ${sPath})
	FOREACH(fileDir ${sDirs})
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
        ENDFOREACH()
	IF ( fileObj_raw )
	    MESSAGE(" ${var} Found at ${fileObj_raw}")
	    BREAK()
	ENDIF()
    ENDFOREACH()
    IF ( fileObj_raw )
        STRING(STRIP ${fileObj_raw} fileObj)
        SET(${var} ${fileObj})
    ELSE()
        SET(${var} "${var}-NOTFOUND")
    ENDIF()
ENDMACRO()


####################################################################
# Dependencies
####################################################################
FIND_PROGRAM(CTEST_COMMAND ctest)
IF( ${CTEST_COMMAND} STREQUAL "CTEST_COMMAND-NOTFOUND" )
    MESSAGE(SEND_ERROR "ctest not found, install it please.")
ENDIF()

FIND_PROGRAM(SELENIUM_SERVER_CMD selenium-server)
IF(${SELENIUM_SERVER_CMD} STREQUAL "SELENIUM_SERVER_CMD-NOTFOUND")
    # find selenium server jar
    FIND_FILE_IN_DIRS(SELENIUM_SERVER_JAR "selenium-server*.jar" "${SELENIUM_SEARCH_PATHS}")
    IF (${SELENIUM_SERVER_JAR} STREQUAL "SELENIUM_SERVER_JAR-NOTFOUND")
        MESSAGE(FATAL_ERROR "selenium-server not found, install it please.")
    ENDIF()
    SET(SELENIUM_SERVER_CMD java -jar ${SELENIUM_SERVER_JAR})
ENDIF()
#MESSAGE("SELENIUM_SERVER_CMD=${SELENIUM_SERVER_CMD}")

### Find the browser binary
FOREACH(_browser ${BROWSERS_TO_TEST})
    FIND_FILE_IN_DIRS(${_browser}_BIN "${${_browser}_BIN_NAME}" "${${_browser}_SEARCH_PATHS}")
    IF("${${_browser}_BIN}" STREQUAL "${_browser}_BIN-NOTFOUND")
	MESSAGE(FATAL_ERROR "Cannot find ${_browser} with ${${_browser}_BIN_NAME}, install it please.")
    ELSE()
	# MESSAGE("${_browser}_BIN=${${_browser}_BIN}")
    ENDIF()
ENDFOREACH()

####################################################################
# Test Suites.
####################################################################

#===================================================================
# Generate test suites.
SET(TEST_ROOT "${CTEST_SOURCE_DIRECTORY}/resources")
#FILE(GLOB_RECURSE TEST_SUITES_RAW  RELATIVE ${TEST_ROOT} "0-*.html")
FILE(GLOB_RECURSE TEST_SUITES_RAW  "0-*.html")

MESSAGE("TEST_SUITES_RAW=${TEST_SUITES_RAW}")
SET(ALL_OUTPUT_TARGETS "")

MACRO(ADD_OUTPUT_FOR_BROWSERS testSuiteName testRole suiteFile)
    FOREACH(browser ${BROWSERS_TO_TEST})
	SET(BROWSER_STR "*${browser} ${${browser}_BIN}")
	FILE(APPEND ${CTESTTEST_CMAKE} "ADD_TEST(\"${testSuiteName}.${testRole}.${browser}\" ${SELENIUM_SERVER_CMD} ${SELENIUM_SERVER_ARG} -htmlsuite \"${BROWSER_STR}\" ${BASE_URL}  ${suiteFile} ${RESULT_DIR}/${testSuiteName}.${testRole}.${browser}.html)\n")
    ENDFOREACH()
ENDMACRO()

SET(TEST_ROLES ADMIN)
MACRO(ADD_OUTPUT_AND_TEST testSuitePath testSuiteName)
    IF (EXISTS "${testSuitePath}/TEST_PRELOGIN")
	SET(_testRoles ${TEST_ROLES} PRELOGIN)
    ELSE()
	SET(_testRoles ${TEST_ROLES})
    ENDIF()
    MESSAGE("testPath=${testSuitePath}")
    FOREACH(testRole ${_testRoles})
	IF (NOT EXISTS "${testSuitePath}/NO_${testRole}" )
	    IF ( ${testRole} STREQUAL "PRELOGIN" )
		SET(suiteFile ${testSuitePath}/0-${testSuiteName}.html)
	    ELSE()
		IF ( ${testRole} STREQUAL "ADMIN" )
		    SET(SISO_TEST_TARGET 2-${testSuiteName}.html)
		    SET(SI_TEST_TARGET   1-${testSuiteName}.html)
		    SET(suiteFile ${testSuitePath}/${SISO_TEST_TARGET})
		ELSE()
		    SET(SISO_TEST_TARGET 4-${testSuiteName}.html)
		    SET(SI_TEST_TARGET   3-${testSuiteName}.html)
		    SET(suiteFile ${testSuitePath}/${SISO_TEST_TARGET})
		ENDIF()
		EXECUTE_PROCESS(COMMAND ${CTEST_SOURCE_DIRECTORY}/generate_test_suite.sh
		    ${testRole} ${testSuitePath} ${testSuiteName} ${SI_TEST_TARGET} ${SISO_TEST_TARGET} ${TEST_ROOT}
		    )
	    ENDIF()
	    ADD_OUTPUT_FOR_BROWSERS(${testSuiteName} "${testRole}" ${suiteFile} )
	ENDIF()
    ENDFOREACH()
ENDMACRO(ADD_OUTPUT_AND_TEST testRole testSuitePath testSuiteName)

#===================================================================
# Write CTestTestfile.cmake

SET(CTESTTEST_CMAKE "${CTEST_BINARY_DIRECTORY}/CTestTestfile.cmake")
FILE(WRITE ${CTESTTEST_CMAKE} "## Generate by CTest\n")

## Generate by CTest\n")
FOREACH(testSuiteRaw ${TEST_SUITES_RAW})
    GET_FILENAME_COMPONENT(testSuitePath ${testSuiteRaw} PATH)
    GET_FILENAME_COMPONENT(testSuiteNameOrig ${testSuiteRaw} NAME_WE)
    STRING(REGEX REPLACE "^0-" "" testSuiteName ${testSuiteNameOrig})

    # Make test rules.
    #    ADD_OUTPUT_AND_TEST(NORMAL ${testSuitePath} ${testSuiteName})
    ADD_OUTPUT_AND_TEST(${testSuitePath} ${testSuiteName})
ENDFOREACH(testSuiteRaw ${TEST_SUITES_RAW})

####################################################################
# See whether publican projects need to be imported."
#
#EXECUTE_PROCESS(COMMAND "${CTEST_SOURCE_DIRECTORY}/import.sh")


