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
class TabValidation extends AbstractValidationAction {
  public id: ValidationId
  public description: string
  public messages: ValidationMessages

  public _sourceExample: string
  public get sourceExample() {
    return "\\t hello world";
  }
  public _targetExample: string
  public get targetExample() {
    return "<span class='js-example__target txt--warning'>missing tab char (\\t)</span> hello world";
  }

  constructor(id: ValidationId, description: string, messages: ValidationMessages) {
    super(id, description, messages)
  }

  public doValidate(source: string, target: string): string[] {
    const errors: string[] = []

    const sourceTabs = source.split('\t')
    const targetTabs = target.split('\t')
    console.log(sourceTabs.length)
    console.log(targetTabs.length)

    if (sourceTabs.length > targetTabs.length) {
      errors.push(this.messages.targetHasFewerTabs)
    } else if (targetTabs.length > sourceTabs.length ) {
      console.log('targetHasMoreTabs')
      errors.push(this.messages.targetHasMoreTabs)
    }
    console.log(errors)

    return errors
  }
}

export default TabValidation
