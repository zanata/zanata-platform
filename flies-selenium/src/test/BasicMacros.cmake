# BasicMacros: Macros which are needed by other modules.
#
# To use: INCLUDE(BasicMacros)
#
#===================================================================
# Macros:
# STRING_TRIM(var str)
#     var: A variable that stores the result.
#     str: A string.
#
# Trim a string. This macro is needed as CMake 2.4 does not support STRING(STRIP ..)
#-------------------------------------------------------------------
# COMMAND_OUTPUT_TO_VARIABLE(var cmd):
#     var: A variable that stores the result.
#     cmd: A command.
#
# Store command output to a variable, without new line characters (\n and \r).
# This macro is suitable for command that output one line result.
# Note that the var will be set to ${var_name}-NOVALUE if cmd does not have
# any output.
#
#-------------------------------------------------------------------
# SETTING_FILE_GET_ATTRIBUTE(var attr_name setting_file [UNQUOTED] [setting_sign]):
#     var: Variable to store the attribute value.
#     attr_name: Name of the attribute.
#     setting_file: Setting filename.
#     setting_sign: (Optional) The symbol that separate attribute name and its value.
#         Default value: "="
#
# Get attribute value from a setting file.
# New line characters will be stripped.
#
#-------------------------------------------------------------------
# DATE_FORMAT(date_var format [locale])
#     date_var: Result date string
#     format: date format for date(1)
#     locale: locale of the string.
#             Use current locale setting if locale is not given.
#
# Get date in specified format and locale.
#
#-------------------------------------------------------------------
# SET_ENV(var default_value [env])
#     var: Variable to be set
#     default_value: Default value of the var
#     env: The name of environment variable. Only need if different from var.
#
# Set the variable and add compiler environment.
#

IF(NOT DEFINED _BASIC_MACROS_CMAKE_)
    SET(_BASIC_MACROS_CMAKE_ "DEFINED")
    IF(NOT DEFINED READ_TXT_CMD)
	SET(READ_TXT_CMD cat)
    ENDIF(NOT DEFINED READ_TXT_CMD)

    MACRO(STRING_TRIM var str)
	IF ("${ARGN}" STREQUAL "UNQUOTED")
	    SET(_unquoted 1)
	ELSE("${ARGN}" STREQUAL "UNQUOTED")
	    SET(_unquoted 0)
	ENDIF("${ARGN}" STREQUAL "UNQUOTED")
	SET(_var_1 "\r${str}\r")
	STRING(REPLACE  "\r[ \t]*" "" _var_2 "${_var_1}" )
	STRING(REGEX REPLACE  "[ \t\r\n]*\r" "" _var_3 "${_var_2}" )
	IF (${_unquoted})
	    STRING(REGEX REPLACE "^\"" "" _var_4 "${_var_3}" )
	    STRING(REGEX REPLACE "\"$" "" ${var} "${_var_4}" )
	ELSE(${_unquoted})
	    SET(${var} "${_var_3}")
        ENDIF(${_unquoted})
    ENDMACRO(STRING_TRIM var str)

    MACRO(COMMAND_OUTPUT_TO_VARIABLE var cmd)
	EXECUTE_PROCESS(
	    COMMAND ${cmd} ${ARGN}
	    OUTPUT_VARIABLE _cmd_output
	    )
	IF(_cmd_output)
	    STRING_TRIM(${var} ${_cmd_output})
	ELSE(_cmd_output)
	    SET(var "${var}-NOVALUE")
	ENDIF(_cmd_output)
	# MESSAGE("var=${var} _cmd_output=${_cmd_output}")
    ENDMACRO(COMMAND_OUTPUT_TO_VARIABLE var cmd)

    MACRO(SETTING_FILE_GET_ATTRIBUTE var attr_name setting_file)
	SET(setting_sign "=")
	SET(_UNQUOTED "")
	FOREACH(_arg ${ARGN})
	    IF (${_arg} STREQUAL "UNQUOTED")
		SET(_UNQUOTED "UNQUOTED")
	    ELSE(${_arg} STREQUAL "UNQUOTED")
	        SET(setting_sign ${_arg})
	    ENDIF(${_arg} STREQUAL "UNQUOTED")
	ENDFOREACH(_arg)
	SET(_find_pattern "\n[ \\t]*${attr_name}[ \\t]*${setting_sign}[^\n]*")
	SET(_ignore_pattern "[ \\t]*${attr_name}[ \\t]*${setting_sign}")

	FILE(READ ${setting_file} _txt_content)
	SET(_txt_content "\n${_txt_content}\n")
	STRING(REGEX MATCHALL "${_find_pattern}" _matched_lines "${_txt_content}")
	MESSAGE("attr_name=${attr_name} _matched_lines=|${_matched_lines}|")
	SET(_result_line)

	# Last line should get priority.
	FOREACH(_line ${_matched_line})
	    SET(_result_line _line)
	ENDFOREACH()

	#MESSAGE("### _result_line=${_result_line}|")
	IF(_result_line)
	    STRING_TRIM(${var} "${_result_line}" ${_UNQUOTED})
	ELSEIF("${_result_line}" EQUAL "0")
	    SET(${var} "0")
	ELSE(_result_line)
	    SET(${var} "")
	ENDIF(_result_line)
	#SET(value "${${var}}")
	#MESSAGE("### ${var}=${value}|")
    ENDMACRO(SETTING_FILE_GET_ATTRIBUTE var attr_name setting_file)

    MACRO(DATE_FORMAT date_var format)
	SET(_locale ${ARGV2})
	IF(_locale)
	    SET(ENV{LC_ALL} ${_locale})
	ENDIF(_locale)
	COMMAND_OUTPUT_TO_VARIABLE(${date_var} date "${format}")
    ENDMACRO(DATE_FORMAT date_var format)

    MACRO(SET_ENV var default_value)
	SET(_env ${ARGV2})
	SET(value ${${var}})
	IF(_env)
	    SET(env ${_env})
	ELSE()
	    SET(env ${var})
	ENDIF()
	IF(NOT DEFINED value)
	    SET(value "${default_value}")
	    SET(${var} "${value}")
	ENDIF(NOT DEFINED value)
	ADD_DEFINITIONS(-D${env}='"${value}"')
    ENDMACRO(SET_ENV var default_value env)

ENDIF(NOT DEFINED _BASIC_MACROS_CMAKE_)

