/* global describe it expect */

import PrintfXSIExtensionValidation from './PrintfXSIExtensionValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

const id = ValidationId.XML_ENTITY
const description = ''
const messageData = en
const PrintfXSIExtensionValidator =
  new PrintfXSIExtensionValidation(id, description, messageData)

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
    expect(errorList).toEqual([
      messageData.mixVarFormats,
      messageData.varsAdded + '%lu',
      messageData.varsMissing + '%3$lu'])
    expect(errorList.length).toEqual(3)
  })
  it('positionalVariableOutOfRange', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%3$s：Read error while reading %99$lu bytes，at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual([
      messageData.varPositionOutOfRange + '%99$lu',
      messageData.varsMissing + ['%1$s', '%3$lu'],
      messageData.varsAdded + ['%3$s', '%99$lu']
    ])
    expect(errorList.length).toEqual(3)
  })
  it('positionalVariablesHaveSamePosition', () => {
    const source = '%s: Read error at byte %s, while reading %lu byte'
    const target = '%3$s：Read error while reading %3$lu bytes, at %2$s'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual([
      messageData.varsMissing + '%1$s',
      messageData.varsAdded + '%3$s',
      messageData.varPositionDuplicated + ['%3$s', '%3$lu']
    ])
    expect(errorList.length).toEqual(3)
  })
  it('invalidPositionalVariablesBringItAll', () => {
    const source = '%s of %d and %lu'
    const target = '%2$d %2$s %9$lu %z'
    const errorList = PrintfXSIExtensionValidator.doValidate(source, target)
    expect(errorList).toEqual([
      messageData.varPositionOutOfRange + '%9$lu',
      messageData.mixVarFormats,
      messageData.varPositionDuplicated + ['%2$d', '%2$s'],
      messageData.varsMissing + ['%1$s', '%3$lu'],
      messageData.varsAdded + ['%2$s', '%9$lu', '%z']
    ])
    expect(errorList.length).toEqual(5)
  })
})
