/* global describe expect it */

import XmlEntityValidation from './XmlEntityValidation'
import ValidationId from '../ValidationId'
// TODO: Consume as react-intl JSON messages file
import Messages from '../messages'

const id = ValidationId.XML_ENTITY
const description = ''
const messageData = Messages['en-US']
const XmlEntityValidator = new XmlEntityValidation(id, description, messageData)

const noErrors = []

describe('XmlEntityValidation', () => {
  it('testNoEntity', () => {
    const source = 'Source string without xml entity'
    const target = 'Target string without xml entity'
    const errorList = XmlEntityValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('testWithCompleteEntity', () => {
    const source = 'Source string'
    const target = 'Target string: &mash; bla bla &test;'
    const errorList = XmlEntityValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('testWithIncompleteEntityCharRef', () => {
    const source = 'Source string'
    const target = 'Target string: &mash bla bla &test'
    const errorList = XmlEntityValidator.doValidate(source, target)
    expect(errorList.length).toEqual(2)
    // assertThat(errorList).contains(messages.invalidXMLEntity("&mash"),
    //   messages.invalidXMLEntity("&test"))
  })
  it('testWithIncompleteEntityDecimalRef', () => {
    const source = 'Source string'
    const target = 'Target string: &#1234 bla bla &#BC;'
    const errorList = XmlEntityValidator.doValidate(source, target)
    expect(errorList.length).toEqual(2)
    // assertThat(errorList).contains(messages.invalidXMLEntity("&#1234"),
    //   messages.invalidXMLEntity("&#BC;"))
  })
  it('testWithIncompleteEntityHexadecimalRef', () => {
    const source = 'Source string'
    const target = 'Target string: &#x1234 bla bla &#x09Z'
    const errorList = XmlEntityValidator.doValidate(source, target)
    expect(errorList.length).toEqual(2)
    // assertThat(errorList).contains(messages.invalidXMLEntity("&#x1234"),
    //   messages.invalidXMLEntity("&#x09Z"))
  })
})
