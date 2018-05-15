/* global describe it expect */

import PrintfVariablesValidation from './PrintfVariablesValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

const id = ValidationId.XML_ENTITY
const description = ''
const messageData = en
const PrintfVariablesValidator =
  new PrintfVariablesValidation(id, description, messageData)

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
    // assertThat(errorList)
    //             .contains(messages.varsMissing(Arrays.asList('%1v')))
    expect(errorList.length).toEqual(1)
  })

  it('missingVarsThroughoutTarget', () => {
    const source = '%a variables in all parts %b of the string %c'
    const target = 'Testing string with no variables'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList)
    //   .contains(messages.varsMissing(Arrays.asList('%a', '%b', '%c')))
    expect(errorList.length).toEqual(1)
  })
  it('addedVarInTarget', () => {
    const source = 'Testing string with no variables'
    const target = 'Testing string with variable %2$#x'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList)
    //   .contains(messages.varsAdded(Arrays.asList('%2$#x')))
    expect(errorList.length).toEqual(1)
  })
  it('addedVarsThroughoutTarget', () => {
    const source = 'Testing string with no variables'
    const target =
      '%1$-0lls variables in all parts %2$-0hs of the string %3$-0ls'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsAdded(Arrays.asList(
    //   '%1$-0lls', '%2$-0hs', '%3$-0ls')))
    expect(errorList.length).toEqual(1)
  })

  it('bothAddedAndMissingVars', () => {
    const source = 'String with %x and %y only, not z'
    const target = 'String with %y and %z, not x'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsAdded(Arrays.asList('%z')),
    //   messages.varsMissing(Arrays.asList('%x')))
    expect(errorList.length).toEqual(2)
  })

  it('substringVariablesDontMatch', () => {
    const source = '%ll'
    const target = '%l %ll'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsAdded(Arrays.asList('%l')))
    expect(errorList.length).toEqual(1)
  })

  it('superstringVariablesDontMatch', () => {
    const source = '%l %ll'
    const target = '%ll'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsMissing(Arrays.asList('%l')))
    expect(errorList.length).toEqual(1)
  })

  it('superstringVariablesDontMatch2', () => {
    const source = '%z'
    const target = '%zz'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList)
    //   .contains(messages.varsMissing(Arrays.asList('%z')),
        // messages.varsAdded(Arrays.asList('%zz')))
    expect(errorList.length).toEqual(2)
  })
  it('checkWithRealWorldExamples', () => {
    // examples from strings in translate.zanata.org
    const source = '%s %d %-25s %r'
    const target = 'no variables'
    const errorList = PrintfVariablesValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.varsMissing(Arrays.asList('%s',
    //   '%d', '%-25s', '%r')))
    expect(errorList.length).toEqual(1)
  })
})
