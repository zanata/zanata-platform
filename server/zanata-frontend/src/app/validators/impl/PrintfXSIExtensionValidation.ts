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

/* tslint:disable:max-line-length*/

import PrintfVariablesValidation from './PrintfVariablesValidation'
import ValidationMessages from '../ValidationMessages'

import MessageFormat from 'intl-messageformat'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class PrintfXSIExtensionValidation extends PrintfVariablesValidation {
  public readonly id = 'PRINTF_XSI_EXTENSION'
  public readonly description: string
  public readonly label: string

  public sourceExample =
    "value must be between %x$1 and %y$2"
  public targetExample =
    "value must be between %x$1 and <span class='js-example__target txt--warning'>%y$3</span>";

  private POSITIONAL_REG_EXP = new RegExp("%(\\d+\\$).+")

  constructor(locale: string, messages: ValidationMessages) {
    super(locale, messages)
    this.description = messages.xmlEntityValidatorDesc
    this.label = messages[this.id]
  }

  public doValidate(source: string, target: string): string[] {
    const errors: string[] = []
    const sourceVars = this.findVars(source)
    const targetVars = this.findVars(target)
    let appendedSourceVars = sourceVars

    if (this.hasPosition(targetVars)) {
      appendedSourceVars = this.appendPosition(sourceVars)
      const err = this.checkPosition(targetVars, appendedSourceVars.length)
      if (err) { errors.push(...err) }
    }

    let message = this.findMissingVariables(appendedSourceVars, targetVars)
    if (message) {
      errors.push(message)
    }
    message = this.findAddedVariables(appendedSourceVars, targetVars)
    if (message) {
      errors.push(message)
    }
    return errors
  }

  private checkPosition(variables: string[], size: number): string[] {
    interface PosVar {
      position: number,
      testVars: string[]
    }
    const errors: string[] = []
    const posToVars: PosVar[] = []
    for (const testVar of variables) {
      const result = this.POSITIONAL_REG_EXP.exec(testVar)
      if (result != null) {
        const positionAndDollar = result[1]
        const position = this.extractPositionIndex(positionAndDollar)
        if (position >= 0 && position < size) {
          const existing = posToVars.find((pos) => pos.position === position)
          if (existing) {
            existing.testVars.push(testVar)
          } else {
            const testVars = [testVar]
            posToVars.push({ position, testVars })
          }
        } else {
          const outofrange = new MessageFormat(this.messages.varPositionOutOfRange, this.locale)
            .format({ outofrange: testVar })
          errors.push(outofrange)
        }
      } else {
        errors.push(this.messages.mixVarFormats)
      }
    }

    if (posToVars.keys.length !== variables.length) {
      // has some duplicate positions
      // TODO is this meant to be string[] or string[][] ?
      const samePosErrors = []
      for (const entry of posToVars) {
        if (entry.testVars.length > 1) {
          samePosErrors.push(entry.testVars)
        }
      }
      if (samePosErrors.length > 0) {
        const samepos = new MessageFormat(this.messages.varPositionDuplicated, this.locale)
          .format({ samepos: samePosErrors })
        errors.push(samepos)
      }
    }
    return errors
  }

  private hasPosition(variables: string[]): boolean {
    for (const testVar of variables) {
      const result = this.POSITIONAL_REG_EXP.exec(testVar)
      if (result != null) {
        return true
      }
    }
    return false
  }

  private appendPosition(sourceVars: string[]): string[] {
    const result: string[] = []
    const regex = this.buildPosRegex(sourceVars.length)
    for (const i in sourceVars) {
      if (sourceVars[i].match(regex)) {
        result.push(sourceVars[i])
      } else {
        const position = Number(i) + 1
        const replacement = `%${position}$`
        result.push(sourceVars[i].replace("%", replacement))
      }
    }
    return result
  }

  private buildPosRegex(size: number): string {
    const numeric = "[1-" + (size + 1) + "]"
    return `.*%${numeric}+\\$.*`
  }

  private extractPositionIndex(positionAndDollar: string): number {
    try {
      return Number(positionAndDollar.substring(0, positionAndDollar.length - 1)) - 1
    } catch (e) {
      return -1
    }

  }
}

export default PrintfXSIExtensionValidation
