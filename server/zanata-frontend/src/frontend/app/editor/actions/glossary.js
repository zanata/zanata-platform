/**
 * Actions related to the glossary.
 */

import { debounce, isEmpty } from 'lodash'
import { CALL_API } from 'redux-api-middleware'

import { baseRestUrl } from '../api'
import { waitForPhraseDetail } from '../utils/phrase'
import { getJsonWithCredentials } from '../utils/api-util'

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
      [CALL_API]: getJsonWithCredentials({
        endpoint: glossaryUrl,
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
      })
    })
  }
}

/* Indicates the index in glossary results that should have details displayed */
export const SET_GLOSSARY_DETAILS_INDEX = Symbol('SET_GLOSSARY_DETAILS_INDEX')
function setGlossaryDetailsIndex (index) {
  return {
    type: SET_GLOSSARY_DETAILS_INDEX,
    payload: { index }
  }
}

/* Set whether glossary details modal is showing */
export const SHOW_GLOSSARY_DETAILS = Symbol('SHOW_GLOSSARY_DETAILS')
export function showGlossaryDetails (show) {
  return {
    type: SHOW_GLOSSARY_DETAILS,
    payload: { show }
  }
}

/**
 * Show the glossary details modal and fetch details from the API.
 *
 * @param index position in results of term to fetch and show details for
 */
export function showGlossaryTermDetails (index) {
  return (dispatch, getState) => {
    const term = getState().glossary.results[index]
    dispatch(setGlossaryDetailsIndex(index))
    dispatch(showGlossaryDetails(true))
    dispatch(getGlossaryDetails(term))
  }
}

/* API request for glossary details has started. */
export const GLOSSARY_DETAILS_REQUEST = Symbol('GLOSSARY_DETAILS_REQUEST')
/* API request for glossary details has completed successfully. */
export const GLOSSARY_DETAILS_SUCCESS = Symbol('GLOSSARY_DETAILS_SUCCESS')
/* API request for glossary details has failed. */
export const GLOSSARY_DETAILS_FAILURE = Symbol('GLOSSARY_DETAILS_FAILURE')

/**
 * Fetch details from the API for all a term's sources for current locales.
 *
 * @param term containing sourceIdList used to perform the lookup.
 */
function getGlossaryDetails (term) {
  return (dispatch, getState) => {
    const { sourceIdList } = term
    const transLocale = getState().headerData.context.selectedLocale

    const termIdsQuery = 'termIds=' + sourceIdList.join('&termIds=')
    const glossaryDetailsUrl =
      `${baseRestUrl}/glossary/details/${transLocale}?${termIdsQuery}`

    dispatch({
      [CALL_API]: getJsonWithCredentials({
        endpoint: glossaryDetailsUrl,
        types: [
          GLOSSARY_DETAILS_REQUEST,
          {
            type: GLOSSARY_DETAILS_SUCCESS,
            meta: { sourceIdList }
          },
          {
            type: GLOSSARY_DETAILS_FAILURE
          }
        ]
      })
    })
  }
}
