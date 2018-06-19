/**
 * Translated Messages for the validators module only.
 * Separate from the frontend src/messages files, these files are formatted with the
 * intl-messageformat library: https://github.com/yahoo/intl-messageformat
 */
import en from './en.json'
import ja from './ja.json'
import fr from './fr.json'
import ValidationMessages from '../ValidationMessages'

const enDefault: ValidationMessages = en

// Shallow extention of translations
// Untranslated messages default to english
const jaExtend: ValidationMessages = { ...en, ...ja }
const frExtend: ValidationMessages = { ...en, ...fr }

/**
 * Aliases for message JSON files
 * Plural support possible:
 * invalidXMLEntity: "Invalid XML {COUNT, plural, =0 {entity: } =1 {entity: } other {entities: } } {ENTITY}"
 */
const Messages = {
  'en': enDefault,
  'en-US': enDefault,
  'ja': jaExtend,
  'ja-JP': jaExtend,
  'fr': frExtend
}

export default Messages
