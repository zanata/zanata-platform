// TODO: Setup Jest for Validators package, convert to Jest test
/* global jest describe expect it */

import XmlEntityValidation from './XmlEntityValidation'
import ValidationId from '../ValidationId'
import ValidationMessages from '../ValidationMessages'
// TODO: Consume as react-intl JSON messages file
import en from '../en'

class XmlEntityValidationTest {
  public id: ValidationId = ValidationId.HTML_XML
  public description: string = ''
  public messageData: ValidationMessages = en

  public XmlEntityValidator = new XmlEntityValidation(this.id, this.description, this.messageData)

  // Incomplete Xml Entity Pair ( on &amp )
  public source: string = '<note>上を向いて &amp; 歩こう</note>'
  public target: string = '<note>Keep your chin up, &amp walk on</note>'

  public errors = this.XmlEntityValidator.doValidate(
    this.source,
    this.target)

  // Jest expect
  public result = this.errors[0] === 'Invalid XML entity: &amp'
      ? 'XmlEntityValidationTest PASSED'
      : 'XmlEntityValidationTest FAILED'

  constructor() {return this}
}

export default XmlEntityValidationTest
