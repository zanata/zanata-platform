// @ts-nocheck
/**
 * Reducer for glossary search in the editor.
 */
import { handleActions } from 'redux-actions'
import { chain } from 'lodash'
import update from 'immutability-helper'
import {
  GLOSSARY_DETAILS_REQUEST,
  GLOSSARY_DETAILS_SUCCESS,
  GLOSSARY_DETAILS_FAILURE,
  GLOSSARY_SEARCH_TEXT_CHANGE,
  GLOSSARY_TERMS_REQUEST,
  GLOSSARY_TERMS_SUCCESS,
  GLOSSARY_TERMS_FAILURE,
  SET_GLOSSARY_DETAILS_INDEX,
  SHOW_GLOSSARY_DETAILS
} from '../actions/glossary-action-types'

const defaultState = {
  searchText: '',
  searching: false,
  // searchText -> results array
  results: new Map(),
  // FIXME should have a timestamp per set of results in the map
  // this works unless the code is sent back in time to the 1960s or earlier.
  resultsTimestamp: new Date(0),
  details: {
    show: false,
    /* Which glossary result to show detail for. Only valid when show is true */
    resultIndex: 0,
    /* Are details currently being fetched? */
    fetching: false,
    /* detail keyed by id in results[x].sourceIdList */
    byId: {}
  }
}

const glossary = handleActions({
  [GLOSSARY_DETAILS_REQUEST]: state =>
    update(state, { details: { fetching: {$set: true} } }),

  [GLOSSARY_DETAILS_SUCCESS]: (state, { payload, meta: {sourceIdList} }) =>
    update(state, { details: {
      fetching: {$set: false},
      // shallow merge so that incoming detail will completely replace
      // old detail with the same id.
      byId: {$merge: chain(payload)
        // response items match source id at the same index
        .zipWith(sourceIdList, (detail, sourceId) => ({
          ...detail,
          sourceId
        }))
        .keyBy('sourceId')
        .value()
      }
    }
  }),

  [GLOSSARY_DETAILS_FAILURE]: state =>
    update(state, { details: { fetching: {$set: false} } }),

  [GLOSSARY_SEARCH_TEXT_CHANGE]: (state, { payload }) =>
    update(state, {searchText: {$set: payload}}),

  [GLOSSARY_TERMS_REQUEST]: state => update(state, {searching: {$set: true}}),

  [GLOSSARY_TERMS_FAILURE]: (state, { meta: {timestamp} }) =>
    timestamp > state.resultsTimestamp
      ? update(state, {
        searching: {$set: false},
        results: {$set: []},
        resultsTimestamp: {$set: timestamp}
      })
      : state,

  [GLOSSARY_TERMS_SUCCESS]:
    (state, {payload, meta: {searchText, timestamp}}) => {
      if (timestamp <= state.resultsTimestamp) {
        return state
      }
      const newResultsMap = new Map(state.results)
      newResultsMap.set(searchText, payload)
      return update(state, {
        searching: {$set: false},
        results: {$set: newResultsMap},
        resultsTimestamp: {$set: timestamp}
      })
    },

  // TODO consider combining this with SHOW_GLOSSARY_DETAILS since that is
  //      when an index becomes relevant
  [SET_GLOSSARY_DETAILS_INDEX]: (state, { payload }) =>
    update(state, { details: { resultIndex: {$set: payload} } }),

  [SHOW_GLOSSARY_DETAILS]: (state, { payload }) =>
    update(state, { details: { show: {$set: payload} } })
}, defaultState)

export default glossary
