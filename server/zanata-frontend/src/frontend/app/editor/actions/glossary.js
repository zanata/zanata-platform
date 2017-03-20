/**
 * Actions related to the glossary.
 */

import { debounce, isEmpty } from 'lodash'
import { CALL_API } from 'redux-api-middleware'

import { baseRestUrl } from '../api'

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

/* Action to set the current glossary search text. Does not trigger a search. */
export const GLOSSARY_SEARCH_TEXT_CHANGE = Symbol('GLOSSARY_SEARCH_TEXT_CHANGE')
export function glossarySearchTextChange (searchText) {
  return {
    type: GLOSSARY_SEARCH_TEXT_CHANGE,
    text: searchText
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
      payload: {
        totalCount: 0,
        results: []
      },
      meta: { timestamp }
    }
  }

  return (dispatch, getState) => {
    const {
      context,
        headerData
    } = getState()

    const srcLocale = context.sourceLocale.localeId
    const transLocale = headerData.context.selectedLocale

    const glossaryUrl =
      `${baseRestUrl}/glossary/entries?srcLocale=${srcLocale}&transLocale=${transLocale}&filter=${searchText}` // eslint-disable-line max-len

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
