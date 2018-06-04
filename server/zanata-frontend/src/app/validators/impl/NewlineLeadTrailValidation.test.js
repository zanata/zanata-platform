/* global describe it expect */

import NewlineLeadTrailValidation from './NewlineLeadTrailValidation'
import ValidationId from '../ValidationId'
import Messages from '../messages'
const locale = 'en-US'

const id = ValidationId.XML_ENTITY
const NewlineLeadTrailValidator =
  new NewlineLeadTrailValidation(id, Messages[locale], locale)

const noErrors = []

describe('NewlineLeadTrailValidation', () => {
  it('noNewlinesBothMatch', () => {
    const source = 'String without newlines'
    const target = 'Different newline-devoid string'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('bothNewlinesBothMatch', () => {
    const source = '\nString with both newlines\n'
    const target = '\nDifferent newline-infested string\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('missingLeadingNewline', () => {
    const source = '\nTesting string with leading new line'
    const target = 'Different string with the newline removed'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = Messages[locale].leadingNewlineMissing
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('addedLeadingNewline', () => {
    const source = 'Testing string without a leading new line'
    const target = '\nDifferent string with a leading newline added'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = Messages[locale].leadingNewlineAdded
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('missingTrailingNewline', () => {
    const source = 'Testing string with trailing new line\n'
    const target = 'Different string with the newline removed'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = Messages[locale].trailingNewlineMissing
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('addedTrailingNewline', () => {
    const source = 'Testing string without a trailing new line'
    const target = 'Different string with a trailing newline added\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = Messages[locale].trailingNewlineAdded
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('addedBothNewlines', () => {
    const source = 'Testing string with no newlines'
    const target = '\nDifferent string with both added\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = [Messages[locale].leadingNewlineAdded, Messages[locale].trailingNewlineAdded]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })

  it('missingBothNewlines', () => {
    const source = '\nString with both newlines\n'
    const target = 'Other string with no newlines'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = [Messages[locale].leadingNewlineMissing, Messages[locale].trailingNewlineMissing]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })

  it('addedAndMissing1', () => {
    const source = '\nString with only leading newline'
    const target = 'Other string with newline trailing\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = [Messages[locale].leadingNewlineMissing, Messages[locale].trailingNewlineAdded]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })

  it('addedAndMissing2', () => {
    const source = 'String with trailing newline\n'
    const target = '\nOther string with newline leading'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    const errorMessages = [Messages[locale].leadingNewlineAdded, Messages[locale].trailingNewlineMissing]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })
})
