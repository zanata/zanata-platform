/* global jest describe it expect */

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

import suggestionsReducer from './suggestions-reducer'

describe('suggestions-reducer test', () => {
  it('generates initial state', () => {
    expect(suggestionsReducer(undefined, {})).toEqual({
      searchType: 'phrase',
      showDetailModalForIndex: undefined,
      textSearch: {
        loading: false,
        searchStrings: [],
        suggestions: [],
        timestamp: 0
      },
      searchByPhrase: {},
      search: {
        input: {
          text: '',
          focused: false
        }
      }
    })
  })

  it('can begin and end phrase copying', () => {
    const withPhraseSuggestions = suggestionsReducer(undefined, {
      type: PHRASE_SUGGESTIONS_UPDATED,
      payload: {
        phraseId: 'p01',
        loading: false,
        searchStrings: [ 'source', 'sources' ],
        suggestions: [
          {
            sourceContents: [ 'source', 'sources' ],
            targetContents: [ 'target', 'targets' ],
            copying: false,
            relevanceScore: 2,
            similarityPercent: 100
          }, {
            sourceContents: [ 'horse', 'horses' ],
            targetContents: [ 'Pferd', 'Pferde' ],
            copying: false,
            relevanceScore: 1.5,
            similarityPercent: 80
          }
        ],
        timestamp: 12345
      }
    })
    const copying = suggestionsReducer(withPhraseSuggestions, {
      type: PHRASE_SUGGESTION_STARTED_COPYING,
      payload: {
        phraseId: 'p01',
        index: 1
      }
    })
    const doneCopying = suggestionsReducer(copying, {
      type: PHRASE_SUGGESTION_FINISHED_COPYING,
      payload: {
        phraseId: 'p01',
        index: 1
      }
    })
    expect(copying.searchByPhrase['p01'].suggestions[1].copying).toEqual(true)
    expect(doneCopying.searchByPhrase['p01'].suggestions[1].copying)
      .toEqual(false)
  })

  it('can receive phrase suggestions', () => {
    const withPhraseSuggestions = suggestionsReducer(undefined, {
      type: PHRASE_SUGGESTIONS_UPDATED,
      payload: {
        phraseId: 'p01',
        loading: false,
        searchStrings: [ 'source', 'sources' ],
        suggestions: [
          {
            sourceContents: [ 'source', 'sources' ],
            targetContents: [ 'target', 'targets' ],
            copying: false,
            relevanceScore: 2,
            similarityPercent: 100
          }, {
            sourceContents: [ 'horse', 'horses' ],
            targetContents: [ 'Pferd', 'Pferde' ],
            copying: false,
            relevanceScore: 1.5,
            similarityPercent: 80
          }
        ],
        timestamp: 12345
      }
    })
    expect(withPhraseSuggestions.searchByPhrase['p01']).toEqual({
      loading: false,
      searchStrings: [ 'source', 'sources' ],
      suggestions: [
        {
          copying: false,
          relevanceScore: 2,
          similarityPercent: 100,
          sourceContents: [ 'source', 'sources' ],
          targetContents: [ 'target', 'targets' ]
        }, {
          sourceContents: [ 'horse', 'horses' ],
          targetContents: [ 'Pferd', 'Pferde' ],
          copying: false,
          relevanceScore: 1.5,
          similarityPercent: 80
        }
      ],
      timestamp: 12345
    })
  })

  it('can set suggestion search type', () => {
    const text = suggestionsReducer(undefined, {
      type: SET_SUGGESTION_SEARCH_TYPE,
      payload: 'text'
    })
    const phrase = suggestionsReducer(text, {
      type: SET_SUGGESTION_SEARCH_TYPE,
      payload: 'phrase'
    })
    expect(text.searchType).toEqual('text')
    expect(phrase.searchType).toEqual('phrase')
  })

  it('can set index for detail modal to show', () => {
    const showing = suggestionsReducer(undefined, {
      type: SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX,
      payload: 42
    })
    expect(showing.showDetailModalForIndex).toEqual(42)
  })

  it('can change search text', () => {
    const withText = suggestionsReducer(undefined, {
      type: SUGGESTION_SEARCH_TEXT_CHANGE,
      payload: 'get schwifty'
    })
    const withOtherText = suggestionsReducer(withText, {
      type: SUGGESTION_SEARCH_TEXT_CHANGE,
      payload: 'get schwiftier'
    })
    expect(withText.search.input.text).toEqual('get schwifty')
    expect(withOtherText.search.input.text).toEqual('get schwiftier')
  })

  it('can begin and end text suggestion copying', () => {
    const withTextSuggestions = suggestionsReducer(undefined, {
      type: TEXT_SUGGESTIONS_UPDATED,
      payload: {
        loading: false,
        searchStrings: [ 'searching' ],
        suggestions: [
          {
            sourceContents: [ 'source', 'sources' ],
            targetContents: [ 'target', 'targets' ],
            copying: false,
            relevanceScore: 2,
            similarityPercent: 100
          }, {
            sourceContents: [ 'horse', 'horses' ],
            targetContents: [ 'Pferd', 'Pferde' ],
            copying: false,
            relevanceScore: 1.5,
            similarityPercent: 80
          }
        ],
        timestamp: 12345
      }
    })
    const copying = suggestionsReducer(withTextSuggestions, {
      type: TEXT_SUGGESTION_STARTED_COPYING,
      payload: 1
    })
    const doneCopying = suggestionsReducer(copying, {
      type: TEXT_SUGGESTION_FINISHED_COPYING,
      payload: 1
    })
    expect(copying.textSearch.suggestions[1].copying).toEqual(true)
    expect(doneCopying.textSearch.suggestions[1].copying).toEqual(false)
  })

  it('can receive text suggestions', () => {
    const withTextSuggestions = suggestionsReducer(undefined, {
      type: TEXT_SUGGESTIONS_UPDATED,
      payload: {
        loading: false,
        searchStrings: [ 'searching' ],
        suggestions: [
          {
            sourceContents: [ 'source', 'sources' ],
            targetContents: [ 'target', 'targets' ],
            copying: false,
            relevanceScore: 2,
            similarityPercent: 100
          }, {
            sourceContents: [ 'horse', 'horses' ],
            targetContents: [ 'Pferd', 'Pferde' ],
            copying: false,
            relevanceScore: 1.5,
            similarityPercent: 80
          }
        ],
        timestamp: 12345
      }
    })
    expect(withTextSuggestions.textSearch).toEqual({
      loading: false,
      searchStrings: [ 'searching' ],
      suggestions: [
        {
          copying: false,
          relevanceScore: 2,
          similarityPercent: 100,
          sourceContents: [ 'source', 'sources' ],
          targetContents: [ 'target', 'targets' ]
        }, {
          sourceContents: [ 'horse', 'horses' ],
          targetContents: [ 'Pferd', 'Pferde' ],
          copying: false,
          relevanceScore: 1.5,
          similarityPercent: 80
        }
      ],
      timestamp: 12345
    })
  })
})
