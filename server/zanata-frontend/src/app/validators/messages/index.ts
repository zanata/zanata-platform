/**
 * Translated Messages for the validators module only.
 * Separate from the frontend src/messages files, these files are formatted with the
 * intl-messageformat library: https://github.com/yahoo/intl-messageformat
 */
import en from './en.json'
import ja from './ja.json'

/**
 * Aliases for message JSON files
 * Plural support possible:
 * invalidXMLEntity: "Invalid XML {COUNT, plural, =0 {entity: } =1 {entity: } other {entities: } } {ENTITY}"
 */
const Messages = {
  'en': en,
  'en-US': en,
  'ja': ja,
  'ja-JP': ja
}

export default Messages
