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
import ValidationId from '../ValidationId'
import ValidationMessages from '../ValidationMessages'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class PrintfXSIExtensionValidation extends PrintfVariablesValidation {
  public id: ValidationId
  public description: string
  public messages: ValidationMessages

  public _sourceExample: string
  public get sourceExample() {
    return "value must be between %x$1 and %y$2";
  }
  public _targetExample: string
  public get targetExample() {
    return "value must be between %x$1 and <span class='js-example__target txt--warning'>%y$3</span>";
  }

  private POSITIONAL_REG_EXP = new RegExp("%(\\d+\\$).+")

  constructor(id: ValidationId, description: string, messages: ValidationMessages) {
    super(id, description, messages)
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
    message = this.findAddedVariables(sourceVars, targetVars)
    if (message) {
      errors.push(message)
    }
    return errors
  }

  private checkPosition(variables: string[], size: number): string[] {
    interface PosVar {
      position: number,
      testVar: string
    }
    const errors: string[] = []
    const posToVars: PosVar[] = []
    for (const testVar of variables) {
      const result = this.POSITIONAL_REG_EXP.exec(testVar)
      if (result != null) {
        const positionAndDollar = result[1]
        const position = this.extractPositionIndex(positionAndDollar)
        if (position >= 0 && position < size) {
          posToVars.push({position, testVar})
        } else {
          errors.push(this.messages.varPositionOutOfRange + testVar)
        }
      } else {
        errors.push(this.messages.mixVarFormats)
      }
    }
    // FIXME: duplicate position check false positive
    if (posToVars.length !== variables.length) {
      // has some duplicate positions
      for (const entry of posToVars) {
        if (entry.testVar.length > 1) {
          errors.push(this.messages.varPositionDuplicated + entry.testVar)
        }
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
