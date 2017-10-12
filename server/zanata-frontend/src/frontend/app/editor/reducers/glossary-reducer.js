/**
 * Reducer for glossary search in the editor.
 */

import { chain } from 'lodash'
import updateObject from 'immutability-helper'
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
  // FIXME should have a result per set of results in the map
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

const glossary = (state = defaultState, action) => {
  switch (action.type) {
    case GLOSSARY_DETAILS_REQUEST:
      return update({
        details: {
          fetching: {$set: true}
        }
      })

    case GLOSSARY_DETAILS_SUCCESS:
      return update({
        details: {
          fetching: {$set: false},
          // shallow merge so that incoming detail will completely replace
          // old detail with the same id.
          byId: {$merge: chain(action.payload)
            // response items match source id at the same index
            .zipWith(action.meta.sourceIdList, (detail, sourceId) => ({
              ...detail,
              sourceId
            }))
            .keyBy('sourceId')
            .value()
          }
        }
      })

    case GLOSSARY_DETAILS_FAILURE:
      return update({
        details: {
          fetching: {$set: false}
        }
      })

    case GLOSSARY_SEARCH_TEXT_CHANGE:
      return update({searchText: {$set: action.payload}})

    case GLOSSARY_TERMS_REQUEST:
      return update({searching: {$set: true}})

    case GLOSSARY_TERMS_FAILURE:
      if (action.meta.timestamp > state.resultsTimestamp) {
        return update({
          searching: {$set: false},
          results: {$set: []},
          resultsTimestamp: {$set: action.meta.timestamp}
        })
      } else {
        return state
      }

    case GLOSSARY_TERMS_SUCCESS:
      if (action.meta.timestamp > state.resultsTimestamp) {
        const newResultsMap = new Map(state.results)
        newResultsMap.set(action.meta.searchText, action.payload)
        return update({
          searching: {$set: false},
          results: {$set: newResultsMap},
          resultsTimestamp: {$set: action.meta.timestamp}
        })
      } else {
        return state
      }

    // TODO consider combining this with SHOW_GLOSSARY_DETAILS since that is
    //      when an index becomes relevant
    case SET_GLOSSARY_DETAILS_INDEX:
      return update({
        details: {
          resultIndex: {$set: action.payload}
        }
      })

    case SHOW_GLOSSARY_DETAILS:
      return update({
        details: {
          show: {$set: action.payload}
        }
      })

    default:
      return state
  }

  /**
   * Apply the given commands to state.
   *
   * Just a shortcut to avoid having to pass state to update over and over.
   */
  function update (commands) {
    return updateObject(state, commands)
  }
}

export default glossary
