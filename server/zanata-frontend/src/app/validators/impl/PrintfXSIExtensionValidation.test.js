/* global describe it expect */
/* eslint-disable max-len */
import PrintfXSIExtensionValidation from './PrintfXSIExtensionValidation'
import Messages from '../messages'
import MessageFormat from 'intl-messageformat'
const locale = 'en-US'

const messages = Messages[locale]

const PrintfXSIExtensionValidator =
  new PrintfXSIExtensionValidation(locale, messages)

const noErrors = []

describe('PrintfXSIExtensionValidation', () => {
  it('validImplicitPositionalVariables', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%1$s：Read error while reading %3$lu bytes，at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('validExplicitPositionalVariables', () => {
    const source = '%1$s: Read error at byte %2$s, while reading %3$lu byte'
    const target = '%1$s：Read error while reading %3$lu bytes，at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('validExplicitPositionalVariables2', () => {
    const source = '%2$.3f%1$s/day'
    const target = '%2$.3f%1$s/jour'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('mixPositionalVariablesWithNotPositional', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%1$s：Read error while reading %lu bytes，at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.mixVarFormats, locale)
        .format()
    const msg2 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%3$lu'] })
    const msg3 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%lu'] })
    expect(errorList).toEqual([msg1, msg2, msg3])
    expect(errorList.length).toEqual(3)
  })
  it('positionalVariableOutOfRange', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%3$s：Read error while reading %99$lu bytes，at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.varPositionOutOfRange, locale)
        .format({ outofrange: '%99$lu' })
    const msg2 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%1$s', '%3$lu'] })
    const msg3 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%3$s', '%99$lu'] })
    expect(errorList).toEqual([msg1, msg2, msg3])
    expect(errorList.length).toEqual(3)
  })
  it('positionalVariablesHaveSamePosition', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%3$s：Read error while reading %3$lu bytes, at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.varPositionDuplicated, locale)
        .format({ samepos: ['%3$s', '%3$lu'] })
    const msg2 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%1$s'] })
    const msg3 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%3$s'] })
    expect(errorList).toEqual([msg1, msg2, msg3])
    expect(errorList.length).toEqual(3)
  })
  it('invalidPositionalVariablesBringItAll', () => {
    const source = '%s of %d and %lu'
    const target = '%2$d %2$s %9$lu %z'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    const msg1 =
      new MessageFormat(messages.varPositionOutOfRange, locale)
        .format({ outofrange: '%9$lu' })
    const msg2 =
      new MessageFormat(messages.mixVarFormats, locale)
        .format()
    const msg3 =
      new MessageFormat(messages.varPositionDuplicated, locale)
        .format({ samepos: ['%2$d', '%2$s'] })
    const msg4 =
      new MessageFormat(messages.varsMissing, locale)
        .format({ missing: ['%1$s', '%3$lu'] })
    const msg5 =
      new MessageFormat(messages.varsAdded, locale)
        .format({ added: ['%2$s', '%9$lu', '%z'] })
    expect(errorList).toEqual([msg1, msg2, msg3, msg4, msg5])
    expect(errorList.length).toEqual(5)
  })
})
