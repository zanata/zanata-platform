cmake_minimum_required(VERSION 2.6)
####################################################################
# Init Definition
####################################################################
SET(CMAKE_ALLOW_LOOSE_LOOP_CONSTRUCTS ON)

####################################################################
# Project basic information (Place to customize)
####################################################################
PROJECT(flies)
SET(PROJECT_DESCRIPTION "Online translator collaboration system")
# base URL for test to be run at:
SET(BASE_URL "http://hudson.englab.bne.redhat.com/")
SET(FLIES_URL "${BASE_URL}flies")
SET(REST_PATH "seam/resource/restv1/projects/p/")
SET(SELENIUM_SERVER_ARGS )
SET(RESULT_DIR results)
SET(BROWSERS_TO_TEST firefox googlechrome)

#===================================================================
# Flies Projects to be test
SET (SAMPLE_PROJ_DIR    samples)
SET (LANGS              "as-IN,ja-JP,zh-CN,pt-PT")

SET(INIT_ITER		"s1")
SET(INIT_ITER_NAME	"s1name")
SEt(INIT_ITER_DESC	"s1 desc")

# Security Guide F13
SET(PROJ_SECURITY_GUIDE_F13		SecurityGuideF13)
SET(PROJ_SECURITY_GUIDE_F13_REPO_TYPE   svn)
SET(PROJ_SECURITY_GUIDE_F13_URL		"http://svn.fedorahosted.org/svn/securityguide/community/f13")
SET(PROJ_SECURITY_GUIDE_F13_NAME	"Security Guide F13")
SET(PROJ_SECURITY_GUIDE_F13_DESC	"Security Guide for Fedora 13")

# About Fedora F13
SET(PROJ_ABOUT_FEDORA_F13		AboutFedoraF13)
SET(PROJ_ABOUT_FEDORA_F13_REPO_TYPE   git)
SET(PROJ_ABOUT_FEDORA_F13_URL		"git://git.fedorahosted.org/git/docs/about-fedora.git")
SET(PROJ_ABOUT_FEDORA_F13_NAME	"About Fedora 13")
SET(PROJ_ABOUT_FEDORA_F13_DESC	"About Fedora 13")

# Release Note
SET(PROJ_RELEASE_NOTE		ReleaseNote)
SET(PROJ_RELEASE_NOTE_REPO_TYPE	git)
SET(PROJ_RELEASE_NOTE_URL	"git://git.fedorahosted.org/git/docs/release-notes.git")
SET(PROJ_RELEASE_NOTE_NAME	"Fedora Release Note")
SET(PROJ_RELEASE_NOTE_DESC	"Fedora Release Note")

SET(PUBLICAN_PROJECTS PROJ_SECURITY_GUIDE_F13 PROJ_ABOUT_FEDORA_F13 PROJ_RELEASE_NOTE)
#SET(PUBLICAN_PROJECTS PROJ_ABOUT_FEDORA_F13)

#===================================================================
# Generate Targets for publican projects
ADD_CUSTOM_COMMAND(OUTPUT ${SAMPLE_PROJ_DIR}
    COMMAND cmake -E make_directory ${SAMPLE_PROJ_DIR}
)

SET(SAMPLE_PROJ_PATHS )
SET(SAMPLE_PROJ_TARGETS )
FILE(STRINGS "apikey" _APIKEY)
STRING(STRIP "${_APIKEY}" APIKEY)
SET(FLIES_PUBLICAN_COMMON_OPTS --errors --debug --user admin --key ${APIKEY})

FOREACH(pProj ${PUBLICAN_PROJECTS})
    MESSAGE("Building target for ${pProj} in ${${pProj}}")
    IF( ${${pProj}_REPO_TYPE} STREQUAL "svn")
	SET(CLONE_ACTION "co")
	SET(UPDATE_ACTION "up")
    ELSE()
	SET(CLONE_ACTION "clone")
	SET(UPDATE_ACTION "pull")
    ENDIF()

    SET(P_PROJ_DIR "${SAMPLE_PROJ_DIR}/${${pProj}}")

    ADD_CUSTOM_COMMAND(OUTPUT "${P_PROJ_DIR}" "${P_PROJ_DIR}/publican.cfg"}
	COMMAND "${${pProj}_REPO_TYPE}" "${CLONE_ACTION}" "${${pProj}_URL}" "${P_PROJ_DIR}"
	DEPENDS ${SAMPLE_PROJ_DIR}
    )

    ADD_CUSTOM_TARGET(clone_${${pProj}}
	DEPENDS ${P_PROJ_DIR}
    )

    # Remove brand
    ADD_CUSTOM_COMMAND(OUTPUT "${P_PROJ_DIR}/publican.cfg.orig"
	COMMAND mv ${P_PROJ_DIR}/publican.cfg ${P_PROJ_DIR}/publican.cfg.orig
	COMMAND sed -e 's/brand:.*//' ${P_PROJ_DIR}/publican.cfg.orig > ${P_PROJ_DIR}/publican.cfg
	DEPENDS "${P_PROJ_DIR}/publican.cfg"
    )

    ADD_CUSTOM_TARGET(remove_brand_${${pProj}}
	DEPENDS ${P_PROJ_DIR}
    )

    ADD_CUSTOM_COMMAND(OUTPUT "${P_PROJ_DIR}/pot"}
        COMMAND ${PUBLICAN_CMD}  update_pot
        WORKING_DIRECTORY "${P_PROJ_DIR}"
        DEPENDS ${P_PROJ_DIR}/publican.cfg.orig
    )

    ADD_CUSTOM_TARGET(update_pot_${${pProj}}
	DEPENDS ${P_PROJ_DIR}/pot
    )

    ADD_CUSTOM_TARGET(update_src_${${pProj}}
	COMMAND "${${pProj}_REPO_TYPE}" "${UPDATE_ACTION}"
        DEPENDS ${P_PROJ_DIR}
	WORKING_DIRECTORY "${P_PROJ_DIR}"
	COMMENT "Updating pot of publican project ${${pProj}}"
    )

    ADD_CUSTOM_TARGET(update_po_${${pProj}}
        COMMAND ${PUBLICAN_CMD} update_po --langs="${LANGS}"
	DEPENDS ${P_PROJ_DIR}/publican.cfg.orig ${P_PROJ_DIR}/pot
        WORKING_DIRECTORY "${P_PROJ_DIR}"
        COMMENT "Updating po of publican project ${${pProj}}"
    )

    ADD_CUSTOM_TARGET(init_${${pProj}}
        DEPENDS ${SAMPLE_PROJ_DIR}
    )

    ADD_DEPENDENCIES(init_${${pProj}} update_pot_${${pProj}} update_po_${${pProj}})

    ADD_CUSTOM_TARGET(createproj_${${pProj}}
	COMMAND "${FLIES_PUBLICAN_CMD}" createproj ${FLIES_PUBLICAN_COMMON_OPTS} --flies "${FLIES_URL}" --proj "${${pProj}}" --name "${${pProj}_NAME}" --desc "${${pProj}_DESC}"
        DEPENDS ${P_PROJ_DIR}
	WORKING_DIRECTORY "${P_PROJ_DIR}"
    	COMMENT "Project ${${pProj}}: Creating"
	VERBATIM
    )

    ADD_CUSTOM_TARGET(createiter_${${pProj}}
	COMMAND "${FLIES_PUBLICAN_CMD}" createiter ${FLIES_PUBLICAN_COMMON_OPTS} --flies "${FLIES_URL}" --proj "${${pProj}}" --iter "${INIT_ITER}" --name "${INIT_ITER_NAME}" --desc "${INIT_ITER_DESC}"
	DEPENDS ${P_PROJ_DIR}
	WORKING_DIRECTORY "${P_PROJ_DIR}"
	COMMENT "Project ${${pProj}}: Creating an iteration ${INIT_ITER}"
	VERBATIM
    )

    SET(UPLOAD_DEST "${FLIES_URL}/${REST_PATH}${${pProj}}/iterations/i/${INIT_ITER}/documents")

    ADD_CUSTOM_TARGET(upload_${${pProj}}
        COMMAND "${FLIES_PUBLICAN_CMD}" upload ${FLIES_PUBLICAN_COMMON_OPTS} -i --src . --dst "${UPLOAD_DEST}"
	DEPENDS ${P_PROJ_DIR} ${P_PROJ_DIR}/publican.cfg.orig ${P_PROJ_DIR}/pot
	WORKING_DIRECTORY "${P_PROJ_DIR}"
	COMMENT "Project ${${pProj}}: Uploading"
	VERBATIM
    )

#    ADD_CUSTOM_TARGET(download_${${pProj}}
#        COMMAND "${FLIES_PUBLICAN_CMD} download ${FLIES_PUBLICAN_COMMON_OPTS} -e --src . --dst "${UPLOAD_DEST}"
#        DEPENDS ${P_PROJ_DIR}
#        WORKING_DIRECTORY "${P_PROJ_DIR}"
#	COMMENT "Project ${${pProj}}: Downloading"
#	VERBATIM
#    )

    ADD_CUSTOM_TARGET(import_${${pProj}}
	COMMENT "Importing Project ${${pProj}}"
	VERBATIM
    )
    ADD_DEPENDENCIES(import_${${pProj}} createproj_${${pProj}} createiter_${${pProj}} upload_${${pProj}})

    # Keep track of the project paths and targets
    SET(SAMPLE_PROJ_PATHS ${SAMPLE_PROJ_PATHS} "${P_PROJ_DIR}")
    SET(SAMPLE_PROJ_TARGETS ${SAMPLE_PROJ_TARGETS} "init_${${pProj}}")
ENDFOREACH()

ADD_CUSTOM_TARGET(init_projects
    DEPENDS ${SAMPLE_PROJ_PATHS}
)

ADD_DEPENDENCIES(init_projects ${SAMPLE_PROJ_TARGETS})


#===================================================================
# Search Paths
SET(MAVEN_REPOSITORY "$ENV{HOME}/.m2/repository/")
SET(MAVEN_SELENIUM_SERVER_PATH "${MAVEN_REPOSITORY}/org/seleniumhq/selenium/server/selenium-server/")
SET(SELENIUM_SEARCH_PATHS ${MAVEN_SELENIUM_SERVER_PATH} /usr/share/java)

SET(firefox_SEARCH_PATHS /usr/lib64/firefox-3.6 /usr/lib/firefox-3.6 /usr/lib64/firefox-3.5 /usr/lib/firefox-3.5
    /usr/lib64/firefox-3* /usr/lib/firefox-3* /usr/lib64/firefox* /usr/lib/firefox*)

SET(opera_SEARCH_PATHS /usr/lib64/opera /usr/lib/opera /opt/opera)

SET(googlechrome_SEARCH_PATHS /opt/google/chrome)

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
	    MESSAGE(" ${var} Found in ${fileObj_raw}")
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
FIND_PROGRAM(PUBLICAN_CMD publican)
IF( ${PUBLICAN_CMD} STREQUAL "PUBLICAN_CMD-NOTFOUND" )
    MESSAGE(SEND_ERROR "publican not found, install it please.")
ENDIF()

FIND_PROGRAM(FLIES_PUBLICAN_CMD flies-publican)
IF( ${FLIES_PUBLICAN_CMD} STREQUAL "FLIES_PUBLICAN_CMD-NOTFOUND" )
    MESSAGE(SEND_ERROR "flies-publican not found, install it please.")
ENDIF()

FIND_PROGRAM(SELENIUM_SERVER_CMD selenium-server)
IF( ${SELENIUM_SERVER_CMD} STREQUAL "SELENIUM_SERVER_CMD-NOTFOUND" )
    # find selenium server jar
    FIND_FILE_IN_DIRS(SELENIUM_SERVER_JAR "selenium-server*.jar" "${SELENIUM_SEARCH_PATHS}")
    IF ( ${SELENIUM_SERVER_JAR} STREQUAL "SELENIUM_SERVER_JAR-NOTFOUND" )
        MESSAGE(FATAL_ERROR "selenium-server not found, install it please.")
    ENDIF()
    SET(SELENIUM_SERVER_CMD java -jar ${SELENIUM_SERVER_JAR})
ENDIF()
#MESSAGE("SELENIUM_SERVER_CMD=${SELENIUM_SERVER_CMD}")

### Find the browser binary
FIND_FILE_IN_DIRS(firefox_BIN firefox "${firefox_SEARCH_PATHS}")
MESSAGE("firefox_BIN=${firefox_BIN}")

#FIND_FILE_IN_DIRS(googlechrome_BIN chrome "${googlechrome_SEARCH_PATHS}")
#MESSAGE("googlechrome_BIN=${googlechrome_BIN}")

#FIND_FILE_IN_DIRS(opera_BIN opera "${opera_SEARCH_PATHS}")
#MESSAGE("opera_BIN=${opera_BIN}")

####################################################################
# Test Suites.
####################################################################

#===================================================================
# Generate test suites.
SET(TEST_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/resources)
FILE(GLOB_RECURSE TEST_SUITES_RAW  "0-*.html")

#MESSAGE("TEST_SUITES_RAW=${TEST_SUITES_RAW}")
SET(LI_FILE LoginBasicTest.html)
SET(LI_PATTERN_MATCH "Test Suite</b></td></tr>")
SET(LI_PATTERN_REPLACE "${LI_PATTERN_MATCH}\n<tr><td><a href=\"${LI_FILE}\">Login</a></td></tr>")

SET(LO_FILE LogoutBasicTest.html)
SET(LO_PATTERN_MATCH "</tbody>")
SET(LO_PATTERN_REPLACE "<tr><td><a href=\"${LO_FILE}\">Logout</a></td></tr>\n${LO_PATTERN_MATCH}")

FOREACH(testSuiteRaw ${TEST_SUITES_RAW})
    GET_FILENAME_COMPONENT(testSuitePath ${testSuiteRaw} PATH)
    GET_FILENAME_COMPONENT(testSuiteNameOrig ${testSuiteRaw} NAME_WE)

    STRING(REGEX REPLACE "^0-" "" testSuiteName ${testSuiteNameOrig})
    FILE(READ ${testSuiteRaw} testSuite_content)

    STRING(REPLACE "${LI_PATTERN_MATCH}" "${LI_PATTERN_REPLACE}" testSuite_content_LI "${testSuite_content}")
    FILE(WRITE "${testSuitePath}/1-${testSuiteName}.html" ${testSuite_content_LI})
    EXECUTE_PROCESS(COMMAND ln -sf  "${TEST_ROOT}/${LI_FILE}" "${testSuitePath}/${LI_FILE}")

    STRING(REPLACE "${LO_PATTERN_MATCH}" "${LO_PATTERN_REPLACE}" testSuite_content_LO "${testSuite_content_LI}")
    FILE(WRITE "${testSuitePath}/3-${testSuiteName}.html" ${testSuite_content_LO})
    EXECUTE_PROCESS(COMMAND ln -sf  "${TEST_ROOT}/${LO_FILE}" "${testSuitePath}/${LO_FILE}")
ENDFOREACH()

FILE(GLOB_RECURSE TEST_SUITES_AUTO  RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}/resources "3-*.html")
#MESSAGE("TEST_SUITES_AUTO=${TEST_SUITES_AUTO}")


#===================================================================
# Make test rules.
ENABLE_TESTING()

FOREACH(browser ${BROWSERS_TO_TEST} )
    IF (${browser} STREQUAL "firefox")
         SET(BROWSER_STR "*${browser} ${${browser}_BIN}")
    ELSE()
         SET(BROWSER_STR "*${browser}")
    ENDIF()
    FOREACH(testSuite ${TEST_SUITES_AUTO})
        GET_FILENAME_COMPONENT(testSuitePath ${testSuite} PATH)
        GET_FILENAME_COMPONENT(testSuiteNameOrig ${testSuite} NAME_WE)
        STRING(REGEX REPLACE "^[0-9]-" "" testSuiteName ${testSuiteNameOrig})
        ADD_TEST("${testSuiteName}_on_${browser}" ${SELENIUM_SERVER_CMD} ${SELENIUM_SERVER_ARG}
             -htmlsuite "${BROWSER_STR}" "${BASE_URL}"
             ${CMAKE_CURRENT_SOURCE_DIR}/resources/${testSuitePath}/${testSuiteNameOrig}.html
             ${CMAKE_CURRENT_SOURCE_DIR}/${RESULT_DIR}/${testSuiteName}_on_${browser}.result.html )
   ENDFOREACH()
ENDFOREACH()


