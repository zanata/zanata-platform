import HtmlXmlTagValidation from './impl/HtmlXmlTagValidation'
import JavaVariablesValidation from './impl/JavaVariablesValidation'
import NewlineLeadTrailValidation from './impl/NewlineLeadTrailValidation'
import PrintfVariablesValidation from './impl/PrintfVariablesValidation'
import PrintfXSIExtensionValidation from './impl/PrintfXSIExtensionValidation'
import TabValidation from './impl/TabValidation'
import XmlEntityValidation from './impl/XmlEntityValidation'
import Messages from './messages'
import ValidationAction from './ValidationAction'
import ValidationMessages from './ValidationMessages'

/**
 * Factory method to instantiate Validators.
 * The Validator intl messages are standalone to the Validators module.
 *
 * @param locale intl messages locale
 * @returns Instantiated Validator class array
 */
function validatorFactory(locale: string): ValidationAction[] {
  // Default to en locale if intl messages unavailable
  const resolvedLocale = Messages[locale]
    ? locale
    : 'en'
  const resolvedMessages: ValidationMessages = Messages[resolvedLocale]
  return [
    new HtmlXmlTagValidation(resolvedMessages, resolvedLocale),
    new JavaVariablesValidation(resolvedMessages, resolvedLocale),
    new NewlineLeadTrailValidation(resolvedMessages, resolvedLocale),
    new PrintfVariablesValidation(resolvedMessages, resolvedLocale),
    new PrintfXSIExtensionValidation(resolvedMessages, resolvedLocale),
    new TabValidation(resolvedMessages, resolvedLocale),
    new XmlEntityValidation(resolvedMessages, resolvedLocale)
  ]
}

// // Window bind export method
// declare let window: any
// window.validatorFactory = validatorFactory

export default validatorFactory
