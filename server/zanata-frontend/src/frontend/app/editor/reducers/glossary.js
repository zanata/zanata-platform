/**
 * Reducer for glossary search in the editor.
 */

import updateObject from 'react-addons-update'
import { keyBy } from 'lodash'

import {
  GLOSSARY_SEARCH_TEXT_CHANGE,
  GLOSSARY_TERMS_REQUEST,
  GLOSSARY_TERMS_SUCCESS,
  GLOSSARY_TERMS_FAILURE
} from '../actions/glossary'

const defaultState = {
  searchText: '',
  searching: false,
  results: [],
  resultsTimestamp: Date.now()
}

const glossary = (state = defaultState, action) => {
  switch (action.type) {
    case GLOSSARY_SEARCH_TEXT_CHANGE:
      return update({searchText: {$set: action.text}})

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
        return update({
          searching: {$set: false},
          results: {$set: action.payload.results.map(
            ({ glossaryTerms }) => {
              return keyBy(glossaryTerms, (term) => {
                return term.locale
              })
            })
          },
          resultsTimestamp: {$set: action.meta.timestamp}
        })
      } else {
        return state
      }

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
