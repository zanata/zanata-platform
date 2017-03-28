/**
 * Actions related to the glossary.
 */

import { debounce, isEmpty } from 'lodash'
import { CALL_API } from 'redux-api-middleware'

import { baseRestUrl } from '../api'
import { waitForPhraseDetail } from '../utils/phrase'

/* Call as search text changes to trigger a glossary search when the text stops
 * changing. This prevents excessive requests while the user is typing.
 *
 * Note: this cannot be nested in the action creator function since that would
 * use a separate debounce copy so it would not work.
 */
const dispatchFindGlossaryTermsWhenInactive = debounce(
    (dispatch, searchText) => {
      dispatch(findGlossaryTerms(searchText))
    }, 250)

/* Update glossary search text in state and trigger a search when text stops
 * being entered. */
export function glossarySearchTextEntered (searchText) {
  return (dispatch) => {
    dispatch(glossarySearchTextChange(searchText))
    dispatchFindGlossaryTermsWhenInactive(dispatch, searchText)
  }
}

/**
 * Run a glossary search based on the given phrase content when available.
 */
export function findGlossaryTermsByPhraseId (phraseId) {
  return (dispatch, getState) => {
    waitForPhraseDetail(getState, phraseId, (phrase) => {
      dispatch(glossarySearchTextEntered(phrase.sources.join(' ')))
    }, 20, () => {
      console.error('No phrase detail for glossary search after to tries.')
    })
  }
}

/* Action to set the current glossary search text. Does not trigger a search. */
export const GLOSSARY_SEARCH_TEXT_CHANGE = Symbol('GLOSSARY_SEARCH_TEXT_CHANGE')
export function glossarySearchTextChange (searchText) {
  return {
    type: GLOSSARY_SEARCH_TEXT_CHANGE,
    text: searchText
  }
}

export const COPY_GLOSSARY_TERM = Symbol('COPY_GLOSSARY_TERM')
export function copyGlossaryTerm (termTranslation) {
  return {
    type: COPY_GLOSSARY_TERM,
    payload: {
      termTranslation
    }
  }
}

/* API request for glossary search has started. */
export const GLOSSARY_TERMS_REQUEST = Symbol('GLOSSARY_TERMS_REQUEST')
/* API request for glossary search has completed successfully. */
export const GLOSSARY_TERMS_SUCCESS = Symbol('GLOSSARY_TERMS_SUCCESS')
/* API request for glossary search has failed. */
export const GLOSSARY_TERMS_FAILURE = Symbol('GLOSSARY_TERMS_FAILURE')

/* Request glossary terms from the API */
function findGlossaryTerms (searchText) {
  // used for success/failure to ensure the most recent results are used
  const timestamp = Date.now()

  if (isEmpty(searchText)) {
    return {
      type: GLOSSARY_TERMS_SUCCESS,
      payload: [],
      meta: { timestamp }
    }
  }

  return (dispatch, getState) => {
    const { context, headerData } = getState()

    const srcLocale = context.sourceLocale.localeId
    const transLocale = headerData.context.selectedLocale
    const projectSlug = headerData.context.projectVersion.project.slug

    const glossaryUrl =
      `${baseRestUrl}/glossary/search?srcLocale=${srcLocale}&transLocale=${transLocale}&project=${projectSlug}&searchText=${encodeURIComponent(searchText)}&maxResults=15` // eslint-disable-line max-len

    dispatch({
      [CALL_API]: {
        endpoint: glossaryUrl,
        method: 'GET',
        // TODO credentials and headers should be added in a repeatable way
        // e.g. withCredentials({...})
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        types: [
          GLOSSARY_TERMS_REQUEST,
          {
            type: GLOSSARY_TERMS_SUCCESS,
            meta: { timestamp }
          },
          {
            type: GLOSSARY_TERMS_FAILURE,
            meta: { timestamp }
          }
        ]
      }
    })
  }
}
