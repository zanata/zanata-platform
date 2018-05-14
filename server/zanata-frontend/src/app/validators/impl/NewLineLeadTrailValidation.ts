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
class NewlineLeadTrailValidation extends AbstractValidationAction {
  public id: ValidationId
  public description: string
  public messages: ValidationMessages

  public _sourceExample: string
  public get sourceExample() {
    return "\\n hello world with lead new line"
  }
  public _targetExample: string
  public get targetExample() {
    return "<span class='js-example__target txt--warning'>missing \\n</span> hello world with lead new line"
  }

  private leadNewlineRegex = "^\n";
  private endNewlineRegex = "\n$";

  private leadRegExp = new RegExp(this.leadNewlineRegex);
  private endRegExp = new RegExp(this.endNewlineRegex);

  constructor(id: ValidationId, description: string, messages: ValidationMessages) {
    super(id, description, messages)
  }

  public doValidate(source: string, target: string): string[] {
    const errors: string[] = []
    if (this.notShareLeading(source, target)) {
      errors.push(this.messages.leadingNewlineMissing);
    }
    if (this.notShareLeading(target, source)) {
      errors.push(this.messages.leadingNewlineAdded);
    }
    if (this.notShareTrailing(source, target)) {
      errors.push(this.messages.trailingNewlineMissing);
    }
    if (this.notShareTrailing(target, source)) {
      errors.push(this.messages.trailingNewlineAdded);
    }
    return errors
  }

  private notShareTrailing(source: string, target: string): boolean {
    return !this.shareTrailing(source, target);
  }

  private notShareLeading(source: string, target: string): boolean {
    return !this.shareLeading(source, target);
  }

  /**
   * @return false if base has a leading newline and test does not, true
   *         otherwise
   */
  private shareLeading(base: string, test: string): boolean {
    return this.leadRegExp.test(base) ? this.leadRegExp.test(test) : true
    // no newline so can't fail
  }

  /**
   * @return false if base has a trailing newline and test does not, true
   * otherwise
   */
  private shareTrailing(base: string, test: string): boolean {
    return this.endRegExp.test(base) ? this.endRegExp.test(test) : true
    // no newline so can't fail
  }
}

export default NewlineLeadTrailValidation
