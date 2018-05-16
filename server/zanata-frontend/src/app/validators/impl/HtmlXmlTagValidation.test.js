/* global describe expect it */

/* eslint-disable max-len */

import HtmlXmlTagValidation from './HtmlXmlTagValidation'
import ValidationId from '../ValidationId'
// import ValidationMessages from '../ValidationMessages'
// TODO: Consume as react-intl JSON messages file
import Messages from '../messages'

const id = ValidationId.HTML_XML
const description = ''
const messageData = Messages['en-US']

const HtmlXmlTagValidator = new HtmlXmlTagValidation(id, description, messageData)

const noErrors = []

describe('HtmlXmlTagValidation', () => {
  it('matchingHtmlNoError', () => {
    const source = '<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>'
    const target = '<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('matchingXmlNoError', () => {
    const source = '<group><users><user>name</user></users></group>'
    const target = '<group><users><user>name</user></users></group>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    expect(errorList).toEqual(noErrors)
  })
  it('addedTagError', () => {
    const source = '<group><users><user>1</user></users></group>'
    const target = '<group><users><user>1</user></users><foo></group>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    expect(errorList.length).toEqual(1)
  })
  it('addedTagsError', () => {
    const source = '<group><users><user>1</user></users></group>'
    const target = '<foo><group><users><bar><user>1</user></users></group><moo>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    expect(errorList.length).toEqual(3)
  })
  it('missingTagError', () => {
    const source = '<html><title>HTML TAG Test</title><foo><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>'
    const target = '<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.tagsMissing(asList("<foo>")))
    expect(errorList.length).toEqual(1)
  })
  it('missingTagsError', () => {
    const source = '<html><title>HTML TAG Test</title><p><table><tr><td>column 1 row 1</td></tr></table></html>'
    const target = '<title>HTML TAG Test</title><table><tr><td>column 1 row 1</td></tr></table>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.tagsMissing(asList(
    //   "<html>", "<p>", "</html>")));
    expect(errorList.length).toEqual(3)
  })
  it('orderOnlyValidatedWithSameTags', () => {
    const source = '<one><two><three></four></five>'
    const target = '<two></five></four><three><six>'
    const errorList = HtmlXmlTagValidator.doValidate(source, target)
    // assertThat(errorList).contains(messages.tagsMissing(asList("<one>")));
    // assertThat(errorList).contains(messages.tagsAdded(asList("<six>")));
    expect(errorList.length).toEqual(2)
  })
  // FIXME: orderValidation method not discovering violations
  // it('lastTagMovedToFirstError', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '<six><one><two><three></four></five>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList("<six>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('firstTagMovedToLastError', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '<two><three></four></five><six><one>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList("<one>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('tagMovedToMiddleError', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '<two><three><one></four></five><six>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList("<one>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('reversedTagsError', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '<six></five></four><three><two><one>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList(
  //   //   "<two>", "<three>", "</four>", "</five>", "<six>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('reportFirstTagsOutOfOrder', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '</four></five><six><one><two><three>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList(
  //   //   "</four>", "</five>", "<six>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('reportLeastTagsOutOfOrder', () => {
  //   const source = '<one><two><three></four></five><six>'
  //   const target = '<six></four></five><one><two><three>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList(
  //   //   "</four>", "</five>", "<six>")));
  //   expect(errorList.length).toEqual(1)
  // })
  // it('swapSomeTagsError', () => {
  //   const source = '<one><two><three></three></two><four></four></one>'
  //   const target = '<one><two></two><four></three><three></four></one>'
  //   const errorList = HtmlXmlTagValidator.doValidate(source, target)
  //   // assertThat(errorList).contains(messages.tagsWrongOrder(asList(
  //   //   "<three>", "</three>")));
  //   expect(errorList.length).toEqual(1)
  // })
})
