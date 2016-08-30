import { chain, map } from 'lodash'

/* convert from structure used in angular to structure used in react */
// TODO we should change the server response to save us from doing this
//      transformation
export const prepareLocales = (locales) => {
  return chain(locales || [])
      .map(function (locale) {
        return {
          id: locale.localeId,
          name: locale.displayName
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
      'needswork', 'translated', 'approved'
    ])
    .mapValues((numStr) => {
      return parseInt(numStr, 10)
    })
    .value()
}

export const prepareDocs = documents => {
  return map(documents || [], 'name')
}
