/* global describe expect it */

import XmlEntityValidation from './XmlEntityValidation'
import Messages from '../messages'
import MessageFormat from 'intl-messageformat'
const locale = 'en-US'

const messages = Messages[locale]

const XmlEntityValidator =
  new XmlEntityValidation(locale, messages)

// @ts-ignore any
const noErrors = []

describe('XmlEntityValidation', () => {
  it('testNoEntity', () => {
    const source = 'Source string without xml entity'
    const target = 'Target string without xml entity'
    const errorList = XmlEntityValidator.doValidate(source, target)
    // @ts-ignore any
    expect(errorList).toEqual(noErrors)
  })
  it('testWithCompleteEntity', () => {
    const source = 'Source string'
    const target = 'Target string: &mash; bla bla &test;'
    const errorList = XmlEntityValidator.doValidate(source, target)
    // @ts-ignore any
    expect(errorList).toEqual(noErrors)
  })
  it('testWithIncompleteEntityCharRef', () => {
    const source = 'Source string'
    const target = 'Target string: bla bla &test'
    const errorList = XmlEntityValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.invalidXMLEntity, locale)
        .format({ entity: ['&test'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('testWithIncompleteEntityDecimalRef', () => {
    const source = 'Source string'
    const target = 'Target string: &#1234 bla bla &#BC;'
    const errorList = XmlEntityValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.invalidXMLEntity, locale)
        .format({ entity: ['&#1234', '&#BC;'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
  it('testWithIncompleteEntityHexadecimalRef', () => {
    const source = 'Source string'
    const target = 'Target string: &#x1234 bla bla &#x09Z'
    const errorList = XmlEntityValidator.doValidate(source, target)
    const errorMessages =
      new MessageFormat(messages.invalidXMLEntity, locale)
        .format({ entity: ['&#x1234', '&#x09Z'] })
    expect(errorList).toEqual([errorMessages])
    expect(errorList.length).toEqual(1)
  })
})
