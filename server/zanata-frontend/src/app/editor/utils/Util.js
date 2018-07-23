// @ts-nocheck
import { chain, isNaN, map } from 'lodash'

const npluralRegex = /^nplurals\s*=\s*(\d*)\s*;/

/**
 * Extract nplurals value as an integer from a Plural-Forms string.
 *
 * Given a string in the form 'nplurals=x; plural=(y)', extract x
 */
export const parseNPlurals = (pluralFormsString) => {
  const result = npluralRegex.exec(pluralFormsString)
  if (result !== null) {
    const nplurals = parseInt(result[1], 10)
    if (!isNaN(nplurals)) {
      return nplurals
    }
  }
  // Could not find and parse a valid nplurals integer
  return undefined
}

/* convert from structure used in angular to structure used in react */
// TODO we should change the server response to save us from doing this
//      transformation
export const prepareLocales = (locales) => {
  return chain(locales || [])
      .map(function (locale) {
        const nplurals = parseNPlurals(locale.pluralForms)
        return {
          id: locale.localeId,
          name: locale.displayName,
          isRTL: locale.rtl,
          nplurals
        }
      })
      .keyBy('id')
      .value()
}

/**
 * Massage stats data to fit what is expected in this app.
 *
 * - Transform some stat keys to match app expectations
 * - Only include relevant stats
 * - Parse values as integers
 */
export const prepareStats = (statistics) => {
  const messageStats = statistics[1]

  return chain(messageStats)
    .mapKeys((value, key) => {
      switch (key) {
        case 'fuzzy':
          return 'needswork'
        case 'translatedOnly':
          return 'translated'
        default:
          return key
      }
    })
    .pick([
      'total', 'untranslated', 'rejected',
      'needswork', 'translated', 'approved', 'mt'
    ])
    .mapValues((numStr) => {
      return parseInt(numStr, 10)
    })
    .value()
}

export const prepareDocs = documents => {
  return map(documents || [], 'name')
}
