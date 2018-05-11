import XmlEntityValidation from './impl/XmlEntityValidation'
import XmlEntityValidationTest from './impl/XmlEntityValidationTest'
import HtmlXmlTagValidation from './impl/HtmlXmlTagValidation'
import HtmlXmlTagValidationTest from './impl/HtmlXmlTagValidationTest'

declare let window: any
window.XmlEntityValidation = XmlEntityValidation
window.XmlEntityValidationTest = XmlEntityValidationTest
window.HtmlXmlTagValidation = HtmlXmlTagValidation
window.HtmlXmlTagValidationTest = HtmlXmlTagValidationTest
