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
  results: []
}

const glossary = (state = defaultState, action) => {
  switch (action.type) {
    case GLOSSARY_SEARCH_TEXT_CHANGE:
      return update({searchText: {$set: action.text}})

    case GLOSSARY_TERMS_REQUEST:
      return update({searching: {$set: true}})

    case GLOSSARY_TERMS_FAILURE:
      return update({
        searching: {$set: false},
        results: {$set: []}
      })

    case GLOSSARY_TERMS_SUCCESS:
      return update({
        searching: {$set: false},
        results: {$set: action.payload.results.map(
          ({ glossaryTerms }) => {
            return keyBy(glossaryTerms, (term) => {
              return term.locale
            })
          })
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
