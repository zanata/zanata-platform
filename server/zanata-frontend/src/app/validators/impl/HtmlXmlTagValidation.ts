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

import AbstractValidationAction from '../AbstractValidationAction'
import ValidationId from '../ValidationId'
import ValidationMessages from '../ValidationMessages'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class XmlEntityValidation extends AbstractValidationAction {
  public id: ValidationId
  public description: string
  public messages: ValidationMessages

  public _sourceExample: string
  public get sourceExample() {
    return "Pepper &amp;amp; salt"
  }
  public _targetExample: string
  public get targetExample() {
    return "Pepper amp<span class='js-example__target txt--warning'> incomplete entity, missing '& and ;'</span> salt"
  }
  // &amp;, &quot;
  private charRefRegex = "&[:a-z_A-Z][a-z_A-Z0-9.-]*;"
  private charRefExp = new RegExp(this.charRefRegex)

  // &#[numeric]
  private decimalRefRegex = ".*&#[0-9]+;"
  private decimalRefExp = new RegExp(this.decimalRefRegex)

  // &#x[hexadecimal]
  private hexadecimalRefRegex = ".*&#x[0-9a-f_A-F]+;"
  private hexadecimalRefExp = new RegExp(this.hexadecimalRefRegex)

  private ENTITY_START_CHAR = "&"

  constructor(id: ValidationId, description: string, messages: ValidationMessages) {
    super(id, description, messages)
  }

  public doValidate(_source: string, target: string): string[] {
    return this.validateIncompleteEntity(target)
  }

  private validateIncompleteEntity(target: string): string[] {
    const errors: string[] = []

    const words: string[] = target.split(" ").map((v) => v.trim()).filter((s) => !!s)

    for (let word in words) {
      if (word.includes(this.ENTITY_START_CHAR) && word.length > 1) {
        word = this.replaceEntityWithEmptyString(this.charRefExp, word)
        word = this.replaceEntityWithEmptyString(this.decimalRefExp, word)
        word = this.replaceEntityWithEmptyString(this.hexadecimalRefExp, word)

        if (word.includes(this.ENTITY_START_CHAR)) {
          // remove any string that occurs in front
          word = word.substring(word.indexOf(this.ENTITY_START_CHAR))
          errors.push(this.messages.invalidXMLEntity)
        }
      }
    }
    return errors
  }

  /**
   * Replace matched string with empty string
   *
   * @param regex
   * @param s
   * @return
   */
  private replaceEntityWithEmptyString(regex: RegExp, s: string): string {
    // let text = s
    // let result = regex.test(text)
    // while (result != null) {
    //   // replace match entity with empty string
    //   text = text.replace(result.groupValues[0], "")
    //   result = regex.test(text)
    // }
    return s.replace(regex, "")
  }

}

export default XmlEntityValidation
