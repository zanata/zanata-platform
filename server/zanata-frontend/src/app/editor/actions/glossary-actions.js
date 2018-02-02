/**
 * Actions related to the glossary.
 */

import { createAction } from 'typesafe-actions'
import { debounce, isEmpty } from 'lodash'
import { CALL_API_ENHANCED } from '../middlewares/call-api'

import {
  GLOSSARY_SEARCH_TEXT_CHANGE,
  COPY_GLOSSARY_TERM,
  GLOSSARY_TERMS_REQUEST,
  GLOSSARY_TERMS_SUCCESS,
  GLOSSARY_TERMS_FAILURE,
  SET_GLOSSARY_DETAILS_INDEX,
  SHOW_GLOSSARY_DETAILS,
  GLOSSARY_DETAILS_REQUEST,
  GLOSSARY_DETAILS_SUCCESS,
  GLOSSARY_DETAILS_FAILURE
} from './glossary-action-types'

import { apiUrl } from '../../config'
import { waitForPhraseDetail } from '../utils/phrase-util'

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

/* Action to set the current glossary search text. Does not trigger a search. */
export const glossarySearchTextChange =
  createAction(GLOSSARY_SEARCH_TEXT_CHANGE)

/* Update glossary search text in state and trigger a search when text stops
 * being entered. */
export function glossarySearchTextEntered (searchText) {
  return (dispatch) => {
    dispatch(glossarySearchTextChange(searchText))
    dispatchFindGlossaryTermsWhenInactive(dispatch, searchText)
  }
}

const TIMES_TO_POLL_FOR_PHRASE_DETAIL = 20

/**
 * Run a glossary search based on the given phrase content when available.
 */
export function findGlossaryTermsByPhraseId (phraseId) {
  return (dispatch, getState) => {
    waitForPhraseDetail(getState, phraseId, (phrase) => {
      dispatch(glossarySearchTextEntered(phrase.sources.join(' ')))
    }, TIMES_TO_POLL_FOR_PHRASE_DETAIL, () => {
      console.error('No phrase detail for glossary search after 20 tries.')
    })
  }
}

export const copyGlossaryTerm = createAction(COPY_GLOSSARY_TERM)

const MAX_GLOSSARY_TERMS = 15

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
      `${apiUrl}/glossary/search?srcLocale=${srcLocale}&transLocale=${transLocale}&project=${projectSlug}&searchText=${encodeURIComponent(searchText)}&maxResults=${MAX_GLOSSARY_TERMS}` // eslint-disable-line max-len

    dispatch({
      [CALL_API_ENHANCED]: {
        endpoint: glossaryUrl,
        types: [
          GLOSSARY_TERMS_REQUEST,
          {
            type: GLOSSARY_TERMS_SUCCESS,
            meta: { searchText }
          },
          GLOSSARY_TERMS_FAILURE
        ]
      }
    })
  }
}

const setGlossaryDetailsIndex = createAction(SET_GLOSSARY_DETAILS_INDEX)
export const showGlossaryDetails = createAction(SHOW_GLOSSARY_DETAILS)

/**
 * Show the glossary details modal and fetch details from the API.
 *
 * @param index position in results of term to fetch and show details for
 */
export function showGlossaryTermDetails (index) {
  return (dispatch, getState) => {
    const { searchText, results } = getState().glossary
    const term = results.get(searchText)[index]
    dispatch(setGlossaryDetailsIndex(index))
    dispatch(showGlossaryDetails(true))
    dispatch(getGlossaryDetails(term))
  }
}

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
      `${apiUrl}/glossary/details/${transLocale}?${termIdsQuery}`

    dispatch({
      [CALL_API_ENHANCED]: {
        endpoint: glossaryDetailsUrl,
        types: [
          GLOSSARY_DETAILS_REQUEST,
          {
            type: GLOSSARY_DETAILS_SUCCESS,
            meta: { sourceIdList }
          },
          GLOSSARY_DETAILS_FAILURE
        ]
      }
    })
  }
}
