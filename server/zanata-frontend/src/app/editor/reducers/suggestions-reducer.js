import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import {
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

/** @type {import('./state').SuggestionsState} */
const defaultState = {
  searchType: 'phrase',
  showDetailModalForIndex: undefined,
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

const suggestionsReducer = handleActions({
  // could add action.copying and combine started+finished actions
  // @ts-ignore
  [PHRASE_SUGGESTION_FINISHED_COPYING]: (state, {payload: {phraseId, index}}) =>
    update(state, { searchByPhrase: { [phraseId]: { suggestions: { [index]: {
      copying: {$set: false}} } } } }),

  // @ts-ignore
  [PHRASE_SUGGESTION_STARTED_COPYING]: (state, {payload: {phraseId, index}}) =>
    update(state, { searchByPhrase: { [phraseId]: { suggestions: { [index]: {
      copying: {$set: true}} } } } }),

  [PHRASE_SUGGESTIONS_UPDATED]: (state, { payload:
    // @ts-ignore
    {phraseId, loading, searchStrings, suggestions, timestamp}}) =>
    update(state, { searchByPhrase: { [phraseId]:
        // must $set a new object because the key may not yet be defined
        {$set: { loading, searchStrings, suggestions, timestamp }}
      }
    }),

  [SET_SUGGESTION_SEARCH_TYPE]: (state, { payload }) =>
    update(state, {searchType: {$set: payload}}),

  [SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX]: (state, { payload }) =>
    update(state, {showDetailModalForIndex: {$set: payload}}),

  [SUGGESTION_SEARCH_TEXT_CHANGE]: (state, { payload }) =>
    update(state, {search: {input: {text: {$set: payload}}}}),

  [TEXT_SUGGESTION_FINISHED_COPYING]: (state, { payload }) =>
    update(state,
      // @ts-ignore
      {textSearch: {suggestions: {[payload]: {copying: {$set: false}}}}}),

  [TEXT_SUGGESTION_STARTED_COPYING]: (state, { payload }) =>
    update(state,
      // @ts-ignore
      {textSearch: {suggestions: {[payload]: {copying: {$set: true}}}}}),

  [TEXT_SUGGESTIONS_UPDATED]: (state, { payload:
    // @ts-ignore
    {loading, searchStrings, suggestions, timestamp} }) => update(state,
      {textSearch: {
        loading: {$set: loading},
        searchStrings: {$set: searchStrings},
        suggestions: {$set: suggestions},
        timestamp: {$set: timestamp}
      }})
}, defaultState)

export default suggestionsReducer
