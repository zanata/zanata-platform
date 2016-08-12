import fetch from 'isomorphic-fetch'
import { baseRestUrl } from '.'
import { chain, sortBy } from 'lodash'
import { oneLiner } from '../utils/string-utils'

// FIXME structure all the API requests this way
//       (unwrap and transform the content before
//       it is given to the action handler, and let
//       all network and status errors propagate as
//       rejection condition)
/**
 * contents - an array of strings representing each source plural form
 *            (single-element array for singluar strings)
 */
export function getSuggestions (sourceLocale, transLocale, contents) {
  // TODO allow different search types?
  const searchType = 'FUZZY_PLURAL'

  const suggestionsUrl = `${baseRestUrl}/suggestions?from=${sourceLocale}&to=${transLocale}&searchType=${searchType}` // eslint-disable-line max-len

  const request = fetch(suggestionsUrl, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors',
    isArray: true,
    body: JSON.stringify(contents)
  })

  const rawSuggestions = request
    .then(response => {
      if (response.ok) {
        if (!isJsonContentType(response)) {
          console.warn('Did not find application/json content type header in ' +
            'response for suggestions search')
        }
        return response.json()
      } else {
        throw new Error(oneLiner`Failed to get suggestions.
          Status ${response.status} ${response.statusText}`)
      }
    })

  return rawSuggestions.then(sortSuggestions).then(addCopyingProperty)
}

function isJsonContentType (response) {
  const contentType = response.headers.get('content-type')
  return contentType && contentType.indexOf('application/json') !== -1
}

/**
 * Sort suggestions so better matches are at the top, and details are in
 * order from most to least relevant.
 *
 * @param {Suggestion[]} suggestions
 * @return {Suggestion[]} the given suggestions in order.
 */
export function sortSuggestions (suggestions) {
  return chain(suggestions)
    .map(sortDetails)
    .map(addBestMatchScores)
    .sortBy(['similarityPercent', 'bestMatchScore',
             'bestMatchModificationDate', 'relevanceScore'])
    .reverse()
    .value()
}

/**
 * Sort the match details of a suggestion by type and date.
 *
 * @param {Suggestion} suggestion to sort details
 * @return {Suggestion} the given suggestion with details in correct order
 */
function sortDetails (suggestion) {
  var matchDetails = sortBy(suggestion.matchDetails, typeAndDateSort)
  return {
    ...suggestion,
    matchDetails
  }
}

/**
 * Add properties 'bestMatchScore' and 'bestMatchModificationDate' to a
 * suggestion to help with sorting.
 *
 * Higher scores are considered better, since the final results are in
 * descending order.
 *
 * @param {Suggestion} suggestion
 * @return {Suggestion}
 */
function addBestMatchScores (suggestion) {
  var bestMatchModificationDate, bestMatchScore
  var topMatch = suggestion.matchDetails[0]

  if (topMatch.type === 'LOCAL_PROJECT') {
    bestMatchModificationDate = topMatch.lastModifiedDate
    bestMatchScore = topMatch.contentState === 'Translated' ? 0 : 1
  }

  if (topMatch.type === 'IMPORTED_TM') {
    bestMatchModificationDate = topMatch.lastChanged
    bestMatchScore = 2
  }

  return {
    ...suggestion,
    bestMatchScore,
    bestMatchModificationDate
  }
}

// TODO use sortBy when lodash version is increased
/**
 * Return a string that will naturally sort local project details before
 * imported TM details, approved state above translated state, and older
 * modification dates first, in that priority order.
 *
 * @param {MatchDetail} detail
 * @return {string} representation of order that will sort appropriately.
 */
function typeAndDateSort (detail) {
  if (detail.type === 'IMPORTED_TM') {
    return '3' + detail.lastChanged
  }
  if (detail.type === 'LOCAL_PROJECT') {
    if (detail.contentState === 'Translated') {
      return '2' + detail.lastModifiedDate
    }
    if (detail.contentState === 'Approved') {
      return '1' + detail.lastModifiedDate
    }
  }
  // Unrecognized, sort last
  return '9'
}

function addCopyingProperty (suggestions) {
  return suggestions.map(suggestion => {
    return {
      ...suggestion,
      copying: false
    }
  })
}
