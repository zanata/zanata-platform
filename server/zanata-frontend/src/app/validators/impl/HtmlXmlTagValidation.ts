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

/* tslint:disable:max-line-length */

import AbstractValidationAction from '../AbstractValidationAction'
import ValidationId from '../ValidationId'
import ValidationMessages from '../ValidationMessages'

import MessageFormat from 'intl-messageformat'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class HtmlXmlTagValidation extends AbstractValidationAction {
  public id: ValidationId
  public description: string
  public messages: ValidationMessages
  public locale: string

  public _sourceExample: string
  public get sourceExample() {
    return "&lt;p&gt;&lt;strong&gt;Hello world&lt;/strong&gt;&lt;/p&gt;"
  }
  public _targetExample: string
  public get targetExample() {
    return "&lt;p&gt;&lt;strong&gt;Hello world<span class='js-example__target txt--warning'>&lt;/stong&gt;</span>&lt;/p&gt;"
  }

  private tagRegex = "<[^>]+>"

  constructor(id: ValidationId, description: string, messages: ValidationMessages, locale?: string) {
    super(id, description, messages, locale)
  }

  public doValidate(source: string, target: string): string[] {
    let errors: string[] = []
    let foundErrors: string[] = this.listMissing(source, target)
    if (foundErrors.length > 0) {
      const tagsMissing = new MessageFormat(this.messages.tagsMissing, this.locale)
        .format({ missing: foundErrors })
      errors.push(tagsMissing)
    }
    foundErrors = this.listMissing(target, source)
    if (foundErrors.length > 0) {
      const tagsAdded = new MessageFormat(this.messages.tagsAdded, this.locale)
        .format({ added: foundErrors })
      errors.push(tagsAdded)
    }
    if (errors.length <= 0) {
      const sourceTags = this.getTagList(source)
      const targetTags = this.getTagList(target)
      // const outOfOrder = this.orderValidation(sourceTags, targetTags)
      if (sourceTags && targetTags) {
        errors = errors.concat(this.orderValidation(sourceTags, targetTags))
      }
    }
    return errors
  }

  private orderValidation(srcTags: string[], trgTags: string[]): string[] {
    let errors: string[] = []

    let longestRun: string[] = null
    let currentRun: string[] = []

    const src = srcTags
    const trg = trgTags

    for (let i = 0; i < src.length; i++) {
      const token = src[i]
      let srcIndex = i
      const trgIndex = trgTags.indexOf(token)

      if (trgIndex > -1) {
        currentRun = []
        currentRun.push(token)

        let j = trgIndex + 1

        while (j < trg.length && srcIndex < src.length - 1) {
          const nextIndexInSrc = this.findInTail(trg[j], src, srcIndex + 1)
          if (nextIndexInSrc > -1) {
            srcIndex = nextIndexInSrc
            currentRun.push(src[srcIndex])
          }
          j++
        }

        if (currentRun.length === srcTags.length) {
          // must all match
          return errors
        }

        if (longestRun === null || longestRun.length < currentRun.length) {
          longestRun = currentRun
        }
      }
    }

    if (longestRun !== null && longestRun.length > 0) {
      let outOfOrder: string[] = []
      for (const aSrc of src) {
        if (!longestRun.find((s) => s === aSrc)) {
          outOfOrder = outOfOrder.concat(aSrc)
        }
      }
      if (outOfOrder) {
        const tagsOutOfOrder = new MessageFormat(this.messages.tagsWrongOrder, this.locale)
          .format({ unordered: outOfOrder })
        errors = errors.concat(tagsOutOfOrder)
      }
    }
    return errors
  }

  private findInTail(toFind: string, findIn: string[], startIndex: number): number {
    for (let i = startIndex; i < findIn.length; i++ ) {
      if (findIn[i] === toFind) {
        return i
      }
    }
    return -1
  }

  private getTagList(src: string): string[] {
    const regExp = new RegExp(this.tagRegex, 'g')
    return src.match(regExp)
  }

  private listMissing(compareFrom: string, compareTo: string): string[] {
    const regExp = new RegExp(this.tagRegex, 'g')
    let tmp = compareTo
    let unmatched: string[] = []
    let result = regExp.exec(compareFrom)

    while (result) {
      const node = result[0]
      if (!tmp.includes(node)) {
        unmatched = unmatched.concat(node)
      } else {
        const index = tmp.indexOf(node)
        const beforeNode = tmp.substring(0, index)
        const afterNode = tmp.substring(index + node.length)
        // remove matched node from
        tmp = beforeNode + afterNode
      }
      result = regExp.exec(compareFrom)
    }
    return unmatched
  }

}

export default HtmlXmlTagValidation
