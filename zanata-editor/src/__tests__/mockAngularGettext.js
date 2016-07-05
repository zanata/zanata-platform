import { reduce, isUndefined } from 'lodash'

// using quick mock for now as a quick solution because it will
// be replaced with a more React-friendly library anyway
const mockGettextCatalog = {
  // getString substitutes values
  getString: (str, replacements) => {
    return isUndefined(replacements)
      ? str
      : reduce(replacements, (acc, value, placeholder) => {
        return acc.replace('{{' + placeholder + '}}', value)
      }, str)
  }
}

export default mockGettextCatalog
