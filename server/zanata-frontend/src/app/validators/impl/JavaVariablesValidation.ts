/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/* tslint:disable:member-ordering */

import AbstractValidationAction from '../AbstractValidationAction'
import ValidationMessages from '../ValidationMessages'

import MessageFormat from 'intl-messageformat'

 /**
  * Checks for consistent java-style variables between two strings.
  *
  * The current implementation will only check that each argument index is used a
  * consistent number of times. This will be extended in future to check that
  * each argument index is used with the same FormatType.
  *
  * @author David Mason, damason@redhat.com
  * @see http://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
  */
class JavaVariablesValidation extends AbstractValidationAction {
  public readonly id = 'JAVA_VARIABLES'
  public readonly description: string
  public readonly label: string

  public readonly sourceExample =
    "value must be between {0} and {1}"
  public readonly targetExample =
    "value must be between {0} and <span class='js-example__target txt--warning'>{2}</span>"

  protected readonly messages: ValidationMessages

  constructor(locale: string, messages: ValidationMessages) {
    super(locale)
    this.messages = messages
    this.description = messages.javaVariablesValidatorDesc
    this.label = messages[this.id]
  }

  public doValidate(source: string, target: string): string[] {
    const errors: string[] = []
    const sourceInfo = this.analyseString(source)
    const targetInfo = this.analyseString(target)

    // check if any indices are added/missing

    const missing: string[] = []
    const missingQuoted: string[] = []
    const added: string[] = []
    const addedQuoted: string[] = []
    const different: string[] = []

    for (const entry of Array.from(sourceInfo.varCounts.entries())) {
      const key = entry[0]
      const value = entry[1]
      const removed = targetInfo.varCounts.delete(key)
      const targetCount = removed ? value : null
      if (targetCount === null) {
        // @ts-ignore any
        const quotedCount = targetInfo.quotedVarCounts[key]
        if (quotedCount !== null && quotedCount > 0) {
          missingQuoted.push(`{${key}}`)
        } else {
          missing.push(`{${key}}`)
        }
      } else if (value !== targetCount) {
        if (targetInfo.quotedVars.some((k) => k === key)) {
          missingQuoted.push(`{${key}}`)
        } else {
          different.push(`{${key}}`)
        }
      }
    }

    // TODO could warn if they were quoted in original
    for (const entry of Array.from(targetInfo.varCounts.entries())) {
      const key = entry[0]
      if (sourceInfo.quotedVarCounts.has(key)) {
        addedQuoted.push(`{${key}}`)
      } else {
        added.push(`{${key}}`)
      }
    }

    // Sort variable lists to ensure consistent ordering of variables
    // in error messages:
    missing.sort()
    missingQuoted.sort()
    added.sort()
    addedQuoted.sort()
    different.sort()

    const looksLikeMessageFormatString: boolean = !sourceInfo.varCounts

    if (missing.length > 0) {
      const varsMissing = new MessageFormat(this.messages.varsMissing, this.locale)
        .format({ missing })
      errors.push(varsMissing)
    }
    if (looksLikeMessageFormatString && sourceInfo.singleApostrophes !== targetInfo.singleApostrophes) {
      // different number of apos.
      errors.push(this.messages.differentApostropheCount)
    }
    if (looksLikeMessageFormatString && sourceInfo.quotedChars === 0
      && targetInfo.quotedChars > 0) {
      // quoted chars in target but not source
      errors.push(this.messages.quotedCharsAdded)
    }
    if (missingQuoted.length > 0) {
      const varsMissingQuoted = new MessageFormat(this.messages.varsMissingQuoted, this.locale)
        .format({ missing: missingQuoted })
      errors.push(varsMissingQuoted)
    }
    if (added.length > 0) {
      const varsAdded = new MessageFormat(this.messages.varsAdded, this.locale)
        .format({ added })
      errors.push(varsAdded)
    }
    if (addedQuoted.length > 0) {
      const varsAddedQuoted = new MessageFormat(this.messages.varsAddedQuoted, this.locale)
        .format({ added: addedQuoted })
      errors.push(varsAddedQuoted)
    }
    if (different.length > 0) {
      const differentVarCount = new MessageFormat(this.messages.differentVarCount, this.locale)
        .format({ different })
      errors.push(differentVarCount)
    }

    // TODO check if indices are used with the same format types
    // e.g. "You owe me {0, currency}" --> "Du schuldest mir {0, percent}"
    // is not correct
    return errors
  }

  private countIndices(fullVars: string[]): Map<string, number> {
    const argumentIndexCounts = new Map<string, number>()
    for (const fullVar of fullVars) {
      let argIndexEnd = fullVar.indexOf(',')
      argIndexEnd = (argIndexEnd !== -1) ? argIndexEnd : fullVar.length - 1
      const argumentIndex = fullVar.substring(1, argIndexEnd).trim()

      if (argumentIndexCounts.has(argumentIndex)) {
        argumentIndexCounts.set(argumentIndex,
          // @ts-ignore any
          argumentIndexCounts[argumentIndex]!! + 1)
      } else {
        argumentIndexCounts.set(argumentIndex, 1)
      }
    }
    return argumentIndexCounts
  }

  private analyseString(inString: string): StringInfo {
    const descriptor = new StringInfo()

    // stack of opening brace positions, replace if better gwt LIFO
    // collection found
    const openings: number[] = []
    let quotedOpenings: number[] = []

    const escapeChars: string[] = []
    escapeChars.push('\\')

    let isEscaped = false
    let isQuoted = false
    let quotedLength = 0

    // scan for opening brace
    for (let i = 0; i < inString.length; i++) {
      // escaping skips a single character
      if (isEscaped) {
        isEscaped = false
        continue
      }
      // TODO add handling of quoting within SubFormatPatternParts and
      // Strings
      const c = inString[i]
      // begin or end quoted sections
      if (c === '\'') {
        if (isQuoted) {
          if (quotedLength === 0) {
            // don't count doubled quotes
            descriptor.singleApostrophes--
          }
          isQuoted = false
        } else {
          isQuoted = true
          quotedLength = 0
          quotedOpenings = [] // clear array
          descriptor.singleApostrophes++
        }
        continue
      }
      if (isQuoted) {
        quotedLength++
        descriptor.quotedChars++

        // identify quoted variables (not valid variables, identified to
        // warn user)
        if (c === '{') {
          quotedOpenings.push(i)
        } else if (c === '}' && quotedOpenings.length > 0) {
          const removeAt = quotedOpenings.splice(quotedOpenings.length - 1, 1)
          const variable = inString.substring(removeAt[0], i + 1)
          descriptor.quotedVars.push(variable)
        }

        continue
      }
      // identify escape character (intentionally after quoted section
      // handling)
      if (escapeChars.some((ec) => ec === c)) {
        isEscaped = true
        continue
      }
      // identify non-quoted variables
      if (c === '{') {
        openings.push(i)
      } else if (c === '}' && openings.length > 0) {
        const removeAt = openings.splice(openings.length - 1, 1)
        const variable = inString.substring(removeAt[0], i + 1)
        descriptor.vars.push(variable)
      }
    }
    descriptor.varCounts = this.countIndices(descriptor.vars)
    descriptor.quotedVarCounts = this.countIndices(descriptor.quotedVars)

    return descriptor
  }
}

/**
 * Holds information about java variables, quoting etc. for a string.
 */
class StringInfo {
  public quotedChars: number = 0
  public singleApostrophes = 0

  public vars: string[] = []
  public quotedVars: string[] = []

  public varCounts: Map<string, number> = new Map()
  public quotedVarCounts: Map<string, number> = new Map()
}

export default JavaVariablesValidation
