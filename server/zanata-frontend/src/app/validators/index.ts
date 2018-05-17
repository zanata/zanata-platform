import HtmlXmlTagValidation from './impl/HtmlXmlTagValidation'
import JavaVariablesValidation from './impl/JavaVariablesValidation'
import NewlineLeadTrailValidation from './impl/NewlineLeadTrailValidation'
import PrintfVariablesValidation from './impl/PrintfVariablesValidation'
import PrintfXSIExtensionValidation from './impl/PrintfXSIExtensionValidation'
import TabValidation from './impl/TabValidation'
import XmlEntityValidation from './impl/XmlEntityValidation'

import Messages from './messages'
import ValidationId from './ValidationId'
import ValidationMessages from './ValidationMessages'

// declare let window: any
// window.XmlEntityValidation = XmlEntityValidation
// window.HtmlXmlTagValidation = HtmlXmlTagValidation

export {
  Messages,
  ValidationId,
  ValidationMessages,
  HtmlXmlTagValidation,
  JavaVariablesValidation,
  NewlineLeadTrailValidation,
  PrintfVariablesValidation,
  PrintfXSIExtensionValidation,
  TabValidation,
  XmlEntityValidation
}
