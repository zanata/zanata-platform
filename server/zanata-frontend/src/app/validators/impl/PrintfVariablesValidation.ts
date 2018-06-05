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

import AbstractValidationAction from '../AbstractValidationAction'
import ValidationMessages from '../ValidationMessages'

import MessageFormat from 'intl-messageformat'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class PrintfVariablesValidation extends AbstractValidationAction {
  public id = 'PRINTF_VARIABLES'
  public readonly description: string
  public readonly messages: ValidationMessages
  public readonly label: string

  public sourceExample =
    "value must be between %x and %y"
  public targetExample =
    "value must be between %x and <span class='js-example__target txt--warning'>%z</span>";

  private GLOBAL_FLAG = "g";

  // derived from translate toolkit printf style variable matching regex. See:
  // http://translate.svn.sourceforge.net/viewvc/translate/src/trunk/translate/filters/checks.py?revision=17978&view=markup
  private VAR_REGEX = "%((?:\\d+\\$|\\(\\w+\\))?[+#-]*(\\d+)?(\\.\\d+)?(hh|h|ll|l|L|z|j|t)?[\\w%])"

  constructor(messages: ValidationMessages, locale: string) {
    super(locale)
    this.messages = messages
    this.description = messages.printfVariablesValidatorDesc
    this.label = messages[this.id]
  }

  public doValidate(source: string, target: string): string[] {
    const errors: string[] = []
    const sourceVars = this.findVars(source)
    const targetVars = this.findVars(target)
    let messages = this.findMissingVariables(sourceVars, targetVars)
    if (messages) {
      errors.push(messages)
    }
    messages = this.findAddedVariables(sourceVars, targetVars)
    if (messages) {
      errors.push(messages)
    }
    return errors
  }

  protected findMissingVariables(sourceVars: string[],
    targetVars?: string[]): string {
    const missing = this.listMissing(sourceVars, targetVars)
    return (missing.length > 0)
      ? new MessageFormat(this.messages.varsMissing, this.locale)
        .format({ missing })
      : null
  }

  protected findAddedVariables(sourceVars: string[], targetVars?: string[]): string {
    // missing from source = added
    const added = this.listMissing(targetVars, sourceVars)
    // Push as combined incompleteEntry errors
    return (added.length > 0)
      ?  new MessageFormat(this.messages.varsAdded, this.locale)
        .format({ added })
      : null
  }

  protected findVars(inString: string): string[] {
    const vars: string[] = []
    // compile each time to reset index
    const varRegExp = new RegExp(this.VAR_REGEX, this.GLOBAL_FLAG)
    let result = varRegExp.exec(inString)
    while (result != null) {
      vars.push(result[0])
      result = varRegExp.exec(inString)
    }
    return vars
  }

  private listMissing(baseVars: string[], testVars: string[]): string[] {
      const remainingVars = testVars
      const unmatched: string[] = []
      for (const baseVar of baseVars) {
        const index = remainingVars.indexOf(baseVar)
        if (index !== -1) {
          remainingVars.splice(index, 1);
        } else {
          unmatched.push(baseVar)
        }
      }
      return unmatched
  }
}

export default PrintfVariablesValidation
