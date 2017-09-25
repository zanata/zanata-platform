import updateObject from 'immutability-helper'

import {
  DIFF_SETTING_CHANGED,
  PHRASE_SUGGESTION_STARTED_COPYING,
  PHRASE_SUGGESTION_FINISHED_COPYING,
  PHRASE_SUGGESTIONS_UPDATED,
  SET_SUGGESTION_SEARCH_TYPE,
  SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX,
  SUGGESTION_SEARCH_TEXT_CHANGE,
  TEXT_SUGGESTION_STARTED_COPYING,
  TEXT_SUGGESTION_FINISHED_COPYING,
  TEXT_SUGGESTIONS_UPDATED
} from '../actions/suggestions-action-types'

const defaultState = {
  searchType: 'phrase',
  showDetailModalForIndex: undefined,
  showDiff: true,
  textSearch: {
    loading: false,
    searchStrings: [],
    suggestions: [],
    timestamp: 0
  },

  // searchStrings is just the source text that was used for the lookup
  // usually won't change, but could if new source is uploaded
  // { phraseId: { loading, searchStrings, suggestions, timestamp } }
  searchByPhrase: {},

  search: {
    input: {
      text: '',
      focused: false
    }
  }
}

const suggestions = (state = defaultState, action) => {
  switch (action.type) {
    case DIFF_SETTING_CHANGED:
      return update({showDiff: {$set: !state.showDiff}})

    // could add action.copying and combine started+finished actions
    case PHRASE_SUGGESTION_FINISHED_COPYING:
      return update({
        searchByPhrase: {
          [action.payload.phraseId]: {
            suggestions: {
              [action.payload.index]: {copying: {$set: false}}
            }
          }
        }
      })

    case PHRASE_SUGGESTION_STARTED_COPYING:
      return update({
        searchByPhrase: {
          [action.payload.phraseId]: {
            suggestions: {
              [action.payload.index]: {copying: {$set: true}}
            }
          }
        }
      })

    case PHRASE_SUGGESTIONS_UPDATED:
      return update({
        searchByPhrase: {
          // must $set a new object because the key may not yet be defined
          [action.phraseId]: {$set: {
            loading: action.loading,
            searchStrings: action.searchStrings,
            suggestions: action.suggestions,
            timestamp: action.timestamp
          }}
        }
      })

    case SET_SUGGESTION_SEARCH_TYPE:
      return update({searchType: {$set: action.payload}})

    case SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX:
      return update({showDetailModalForIndex: {$set: action.index}})

    case SUGGESTION_SEARCH_TEXT_CHANGE:
      return update({search: {input: {text: {$set: action.payload}}}})

    case TEXT_SUGGESTION_FINISHED_COPYING:
      return update({
        textSearch: {
          suggestions: {[action.payload]: {copying: {$set: false}}}
        }
      })

    case TEXT_SUGGESTION_STARTED_COPYING:
      return update({
        textSearch: {
          suggestions: {[action.payload]: {copying: {$set: true}}}
        }
      })

    case TEXT_SUGGESTIONS_UPDATED:
      return update({textSearch: {
        loading: {$set: action.payload.loading},
        searchStrings: {$set: action.payload.searchStrings},
        suggestions: {$set: action.payload.suggestions},
        timestamp: {$set: action.payload.timestamp}
      }})

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

export default suggestions
