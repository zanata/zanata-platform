/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/* global describe it expect */
/* eslint-disable quotes, max-len */

import JavaVariablesValidation from './JavaVariablesValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import Messages from '../messages'
import MessageFormat from 'intl-messageformat'
const locale = 'en-US'

const id = ValidationId.JAVA_VARIABLES
const description = ''
const JavaVariablesValidator =
  new JavaVariablesValidation(id, description, Messages[locale], locale)

const noErrors = []

describe('TabValidation', () => {
  it('noTabsInEither', () => {
    const source = 'Source without tab'
    const target = 'Target without tab'
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('noErrorForMatchingVars', () => {
    const source = "Testing string with variable {0} and {1}"
    const target = "{1} and {0} included, order not relevant"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('doesNotDetectEscapedVariables', () => {
    const source = "This string does not contain \\{0\\} style variables"
    const target = "This string does not contain java style variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('doesNotDetectQuotedVariables', () => {
    const source = "This string does not contain '{0}' style variables"
    const target = "This string does not contain java style variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('doesNotDetectVariablesInQuotedText', () => {
    const source = "This 'string does not contain {0} style' variables"
    const target = "This string does not contain java style variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('advancedQuoting', () => {
    const source = "'''{'0}'''''{0}'''"
    const target = "From examples on MessageFormat page, should not contain any variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('translatedChoicesStillMatch', () => {
    const source = "There {0,choice,0#are no things|1#is one thing|1<are many things}."
    const target = "Es gibt {0,choice,0#keine Dinge|1#eine Sache|1<viele Dinge}."
    const errorList = JavaVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })

  it('missingVarInTarget', () => {
    const source = "Testing string with variable {0}"
    const target = "Testing string with no variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsMissing(Arrays.asList("{0}")))
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('missingVarsThroughoutTarget', () => {
    const source = "{0} variables in all parts {1} of the string {2}"
    const target = "Testing string with no variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0},{1},{2}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('addedVarInTarget', () => {
    const source = "Testing string with no variables"
    const target = "Testing string with variable {0}"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsAdded, locale)
        .format({ added: '{0}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('addedVarsThroughoutTarget', () => {
    const source = "Testing string with no variables"
    const target = "{0} variables in all parts {1} of the string {2}"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsAdded, locale)
        .format({ added: '{0},{1},{2}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('bothAddedAndMissingVars', () => {
    const source = "String with {0} and {1} only, not 2"
    const target = "String with {1} and {2}, not 0"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0}' })
    const msg2 =
      new MessageFormat(JavaVariablesValidator.messages.varsAdded, locale)
        .format({ added: '{2}' })
    expect(errorList).toEqual([msg1, msg2])
    expect(errorList.length).toEqual(2)
  })

  it('disturbanceInTheForce', () => {
    const source = "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}."
    const target = "At time on date, there was a disturbance in the force on planet Earth"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0},{1},{2}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('diskContainsFiles', () => {
    const source = "The disk \"{1}\" contains {0} file(s)."
    const target = "The disk contains some files"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0},{1}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('ignoresEscapedQuotes', () => {
    const source = "This string does not contain \\'{0}\\' style variables"
    const target = "This string does not contain java style variables"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsMissing(Arrays.asList("{0}")))
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('choiceFormatAndRecursion', () => {
    const source = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}."
    const target = "There are 0 files"
    const errorList = JavaVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(JavaVariablesValidator.messages.varsMissing, locale)
        .format({ missing: '{0}' })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  // TODO tests for format type

  // TODO test 3 or 4 levels of recursion
})
