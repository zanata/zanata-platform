/* global describe it expect */

import TabValidation from './TabValidation'
import MessageFormat from 'intl-messageformat'
import Messages from '../messages'
const locale = 'en-US'

const messages = Messages[locale]

const TabValidator =
  new TabValidation(locale, messages)

// @ts-ignore any
const noErrors = []

describe('TabValidation', () => {
  it('noTabsInEither', () => {
    const source = 'Source without tab'
    const target = 'Target without tab'
    const errorList = TabValidator.doValidate(source, target)
    // @ts-ignore any
    expect(errorList).toEqual(noErrors)
  })
  it('tabsInBoth', () => {
    const source = 'Source with\ttab'
    const target = 'Target with\ttab'
    const errorList = TabValidator.doValidate(source, target)
    // @ts-ignore any
    expect(errorList).toEqual(noErrors)
  })
  it('noTabsInTarget', () => {
    const source = 'Source with\ttab'
    const target = 'Target without tab'
    const errorList = TabValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.targetHasFewerTabs, locale)
      .format({ sourceTabs: 1, targetTabs: 0 })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('noTabsInSource', () => {
    const source = 'Source without tab'
    const target = 'Target with\ttab'
    const errorList = TabValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.targetHasMoreTabs, locale)
        .format({ sourceTabs: 0, targetTabs: 1 })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('fewerTabsInTarget', () => {
    const source = 'Source with two\t\t tabs'
    const target = 'Target with one\ttab'
    const errorList = TabValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.targetHasFewerTabs, locale)
        .format({ sourceTabs: 2, targetTabs: 1 })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('moreTabsInTarget', () => {
    const source = 'Source with one\t tab'
    const target = 'Target with two\t\ttabs'
    const errorList = TabValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.targetHasMoreTabs, locale)
        .format({ sourceTabs: 1, targetTabs: 2 })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
})
