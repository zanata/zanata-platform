/* global describe it expect */

import TabValidation from './TabValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

const id = ValidationId.XML_ENTITY
const description = ''
const messageData = en
const TabValidator = new TabValidation(id, description, messageData)

const noErrors = []

describe('TabValidation', () => {
  it('noTabsInEither', () => {
    const source = 'Source without tab'
    const target = 'Target without tab'
    const errorList = TabValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('tabsInBoth', () => {
    const source = 'Source with\ttab'
    const target = 'Target with\ttab'
    const errorList = TabValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('noTabsInTarget', () => {
    const source = 'Source with\ttab'
    const target = 'Target without tab'
    const errorList = TabValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.targetHasMoreTabs(0, 1))
    expect(errorList.length).toEqual(1)
  })
  it('noTabsInSource', () => {
    const source = 'Source without tab'
    const target = 'Target with\ttab'
    const errorList = TabValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.targetHasMoreTabs(2, 1))
    expect(errorList.length).toEqual(1)
  })
  it('fewerTabsInTarget', () => {
    const source = 'Source with two\t\t tabs'
    const target = 'Target with one\ttab'
    const errorList = TabValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.targetHasMoreTabs(2, 1))
    expect(errorList.length).toEqual(1)
  })
  it('moreTabsInTarget', () => {
    const source = 'Source with one\t tab'
    const target = 'Target with two\t\ttabs'
    const errorList = TabValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.targetHasMoreTabs(1, 2))
    expect(errorList.length).toEqual(1)
  })
})
