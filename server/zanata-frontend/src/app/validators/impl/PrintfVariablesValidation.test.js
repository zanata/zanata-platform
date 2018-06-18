/* global describe it expect */

import PrintfVariablesValidation from './PrintfVariablesValidation'
import Messages from '../messages'
import MessageFormat from 'intl-messageformat'
const locale = 'en-US'

const messages = Messages[locale]

const PrintfVariablesValidator =
  new PrintfVariablesValidation(locale, messages)

const noErrors = []

describe('PrintfVariablesValidation', () => {
  it('noErrorForMatchingVars', () => {
    const source = 'Testing string with variable %1v and %2v'
    const target = '%2v and %1v included, order not relevant'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('missingVarInTarget', () => {
    const source = 'Testing string with variable %1v'
    const target = 'Testing string with no variables'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%1v'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('missingVarsThroughoutTarget', () => {
    const source = '%a variables in all parts %b of the string %c'
    const target = 'Testing string with no variables'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%a', '%b', '%c'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('addedVarInTarget', () => {
    const source = 'Testing string with no variables'
    const target = 'Testing string with variable %2$#x'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%2$#x'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('addedVarsThroughoutTarget', () => {
    const source = 'Testing string with no variables'
    const target =
      '%1$-0lls variables in all parts %2$-0hs of the string %3$-0ls'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%1$-0lls', '%2$-0hs', '%3$-0ls'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('bothAddedAndMissingVars', () => {
    const source = 'String with %x and %y only, not z'
    const target = 'String with %y and %z, not x'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%x'] })
    const msg2 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%z'] })
    const errorMessages = [msg1, msg2]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })

  it('substringVariablesDontMatch', () => {
    const source = '%ll'
    const target = '%l %ll'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%l'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('superstringVariablesDontMatch', () => {
    const source = '%l %ll'
    const target = '%ll'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%l'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })

  it('superstringVariablesDontMatch2', () => {
    const source = '%z'
    const target = '%zz'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%z'] })
    const msg2 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%zz'] })
    const errorMessages = [msg1, msg2]
    expect(errorList).toEqual(errorMessages)
    expect(errorList.length).toEqual(2)
  })
  it('checkWithRealWorldExamples', () => {
    // examples from strings in translate.zanata.org
    const source = '%s %d %-25s %r'
    const target = 'no variables'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.varsMissing, locale)
        .format({
          missing: ['%s', '%d', '%-25s', '%r'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
})
