/* global describe it expect */

import NewlineLeadTrailValidation from './NewlineLeadTrailValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

const id = ValidationId.XML_ENTITY
const description = ''
const messageData = en
const NewlineLeadTrailValidator =
  new NewlineLeadTrailValidation(id, description, messageData)

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
    // assertThat(errorList).contains(messages.leadingNewlineMissing())
    expect(errorList.length).toEqual(1)
  })
  it('addedLeadingNewline', () => {
    const source = 'Testing string without a leading new line'
    const target = '\nDifferent string with a leading newline added'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.leadingNewlineAdded())
    expect(errorList.length).toEqual(1)
  })
  it('missingTrailingNewline', () => {
    const source = 'Testing string with trailing new line\n'
    const target = 'Different string with the newline removed'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.trailingNewlineMissing())
    expect(errorList.length).toEqual(1)
  })
  it('addedTrailingNewline', () => {
    const source = 'Testing string without a trailing new line'
    const target = 'Different string with a trailing newline added\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.trailingNewlineAdded())
    expect(errorList.length).toEqual(1)
  })

  it('addedBothNewlines', () => {
    const source = 'Testing string with no newlines'
    const target = '\nDifferent string with both added\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.leadingNewlineAdded(),
      // messages.trailingNewlineAdded())
    expect(errorList.length).toEqual(2)
  })

  it('missingBothNewlines', () => {
    const source = '\nString with both newlines\n'
    const target = 'Other string with no newlines'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.leadingNewlineMissing(),
      // messages.trailingNewlineMissing())
    expect(errorList.length).toEqual(2)
  })

  it('addedAndMissing1', () => {
    const source = '\nString with only leading newline'
    const target = 'Other string with newline trailing\n'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.leadingNewlineMissing(),
      // messages.trailingNewlineAdded())
    expect(errorList.length).toEqual(2)
  })

  it('addedAndMissing2', () => {
    const source = 'String with trailing newline\n'
    const target = '\nOther string with newline leading'
    const errorList = NewlineLeadTrailValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.leadingNewlineAdded(),
      // messages.trailingNewlineMissing())
    expect(errorList.length).toEqual(2)
  })
})
