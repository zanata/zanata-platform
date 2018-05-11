// TODO: Setup Jest for Validators package, convert to Jest test
/* global jest describe expect it */

/* tslint:disable:max-line-length */

import HtmlXmlTagValidation from './HtmlXmlTagValidation'
import ValidationId from '../ValidationId'
import ValidationMessages from '../ValidationMessages'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

class HtmlXmlTagValidationTest {
  public id: ValidationId = ValidationId.HTML_XML
  public description: string = ''
  public messageData: ValidationMessages = en

  public HtmlXmlTagValidator = new HtmlXmlTagValidation(this.id, this.description, this.messageData)

  constructor() { return this }

  public addedTagError() {
    const source = "<group><users><user>1</user></users></group>";
    const target = "<group><users><user>1</user></users><foo></group>";
    const errors = this.HtmlXmlTagValidator.doValidate(source, target)

    // Jest expect
    return errors[0] === this.messageData.tagsAdded + "<foo>"
      ? 'HtmlXmlTagValidationTest addedTagError PASSED'
      : 'HtmlXmlTagValidationTest addedTagError FAILED'
  }

  public missingTagError() {
    const source = "<html><title>HTML TAG Test</title><foo><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
    const target = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
    const errors = this.HtmlXmlTagValidator.doValidate(source, target)

    // Jest expect
    return errors[0] === this.messageData.tagsMissing + "<foo>"
      ? 'HtmlXmlTagValidationTest missingTagError PASSED'
      : 'HtmlXmlTagValidationTest missingTagError FAILED'
  }

  public orderOnlyValidatedWithSameTags() {
    const source = "<one><two><three></four></five>";
    const target = "<two></five></four><three><six>";
    const errors = this.HtmlXmlTagValidator.doValidate(source, target)

    // Jest expect
    return errors[0] === this.messageData.tagsMissing + "<one>"
      ? 'HtmlXmlTagValidationTest missingTagError PASSED'
      : 'HtmlXmlTagValidationTest missingTagError FAILED'
  }
}

export default HtmlXmlTagValidationTest
