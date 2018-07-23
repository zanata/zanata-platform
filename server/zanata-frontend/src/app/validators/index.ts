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
function createValidators(locale: string): ValidationAction[] {
  // Default to en locale if intl messages unavailable
  // @ts-ignore any
  const resolvedLocale = Messages[locale]
    ? locale
    : 'en'
  // @ts-ignore any
  const resolvedMessages: ValidationMessages = Messages[resolvedLocale]
  return [
    new HtmlXmlTagValidation(resolvedLocale, resolvedMessages),
    new JavaVariablesValidation(resolvedLocale, resolvedMessages),
    new NewlineLeadTrailValidation(resolvedLocale, resolvedMessages),
    new PrintfVariablesValidation(resolvedLocale, resolvedMessages),
    new PrintfXSIExtensionValidation(resolvedLocale, resolvedMessages),
    new TabValidation(resolvedLocale, resolvedMessages),
    new XmlEntityValidation(resolvedLocale, resolvedMessages)
  ]
}

// // Window bind export method
// declare let window: any
// window.createValidators = createValidators

export default createValidators
