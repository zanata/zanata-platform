// @ts-nocheck
/* global jest describe it expect */

import glossaryReducer from './glossary-reducer'
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

describe('glossary-reducer test', () => {
  it('generates initial state', () => {
    const initial = glossaryReducer(undefined, { type: 'an action' })
    expect(initial).toEqual({
      searchText: '',
      searching: false,
      results: new Map(),
      resultsTimestamp: new Date(0),
      details: {
        show: false,
        resultIndex: 0,
        fetching: false,
        byId: {}
      }
    })
  })

  it('can track fetching', () => {
    const initial = glossaryReducer(undefined, { type: 'any' })
    const requested = glossaryReducer(initial, {
      type: GLOSSARY_DETAILS_REQUEST
    })
    expect(requested.details.fetching).toEqual(true)
    const failed = glossaryReducer(requested, {
      type: GLOSSARY_DETAILS_FAILURE
    })
    expect(failed.details.fetching).toEqual(false)
  })

  it('can receive details', () => {
    const requestAction = {
      type: GLOSSARY_DETAILS_REQUEST
    }
    const firstDetails = {
      type: GLOSSARY_DETAILS_SUCCESS,
      meta: {
        sourceIdList: ['a', 'b']
      },
      payload: [{
        description: 'A flying mammal.'
      }, {
        description: 'A talking bird'
      }]
    }
    const secondDetails = {
      type: GLOSSARY_DETAILS_SUCCESS,
      meta: {
        sourceIdList: ['c', 'b']
      },
      payload: [{
        description: 'A walking clock'
      }, {
        description: 'The same talking bird'
      }]
    }

    const initial = glossaryReducer(undefined, { type: 'any' })
    const withFirstRequest = glossaryReducer(initial, requestAction)
    const withFirst = glossaryReducer(withFirstRequest, firstDetails)
    const withSecondRequest = glossaryReducer(withFirst, requestAction)
    const withSecond = glossaryReducer(withSecondRequest, secondDetails)

    expect(withFirstRequest.details.fetching).toEqual(true)
    expect(withFirst.details.fetching).toEqual(false)
    expect(withFirst.details.byId).toEqual({
      'a': {
        sourceId: 'a',
        description: 'A flying mammal.'
      },
      'b': {
        sourceId: 'b',
        description: 'A talking bird'
      }
    })
    expect(withSecondRequest.details.fetching).toEqual(true)
    expect(withSecond.details.fetching).toEqual(false)
    expect(withSecond.details.byId).toEqual({
      'a': {
        sourceId: 'a',
        description: 'A flying mammal.'
      },
      'b': {
        sourceId: 'b',
        description: 'The same talking bird'
      },
      'c': {
        sourceId: 'c',
        description: 'A walking clock'
      }
    })
  })

  it('can change search text', () => {
    const initial = glossaryReducer(undefined, { type: 'any' })
    const withText = glossaryReducer(initial, {
      type: GLOSSARY_SEARCH_TEXT_CHANGE,
      payload: "where's my car?"
    })
    expect(withText.searchText).toEqual("where's my car?")
  })

  // TODO also track the timestamp recording in here
  it('can track searching for terms', () => {
    const highNoon = new Date(2017, 4, 4, 11, 0, 0, 0)
    const initial = glossaryReducer(undefined, { type: 'any' })
    const requested = glossaryReducer(initial, {
      type: GLOSSARY_TERMS_REQUEST
    })
    const failed = glossaryReducer(requested, {
      type: GLOSSARY_TERMS_FAILURE,
      meta: {
        timestamp: highNoon
      }
    })
    expect(requested.searching).toEqual(true)
    expect(failed.searching).toEqual(false)
    expect(failed.resultsTimestamp).toEqual(highNoon)
  })

  it('can receive term search results', () => {
    const afterNoon = new Date(2017, 4, 4, 12, 1, 0, 0)
    const initial = glossaryReducer(undefined, { type: 'any' })
    const requested = glossaryReducer(initial, {
      type: GLOSSARY_TERMS_REQUEST
    })
    const succeded = glossaryReducer(requested, {
      type: GLOSSARY_TERMS_SUCCESS,
      payload: [
        'a', 'b', 'c'
      ],
      meta: {
        timestamp: afterNoon,
        searchText: "where's my car?"
      }
    })
    expect(requested.searching).toEqual(true)
    expect(succeded.searching).toEqual(false)
    expect(succeded.results.get("where's my car?")).toEqual(['a', 'b', 'c'])
    expect(succeded.resultsTimestamp).toEqual(afterNoon)
  })

  it('does not use obsolete search results', () => {
    const earlier = new Date(2017, 4, 4, 10, 0, 0, 0)
    const early = new Date(2017, 4, 4, 11, 0, 0, 0)
    const late = new Date(2017, 4, 4, 13, 0, 0, 0)

    const initial = glossaryReducer(undefined, { type: 'any' })
    const lateSearchSucceeded = glossaryReducer(initial, {
      type: GLOSSARY_TERMS_SUCCESS,
      payload: [ 'recent result' ],
      meta: {
        timestamp: late,
        searchText: 'what are birds?'
      }
    })
    const earlySearchSucceeded = glossaryReducer(lateSearchSucceeded, {
      type: GLOSSARY_TERMS_SUCCESS,
      payload: [ 'obsolete result' ],
      meta: {
        timestamp: early,
        searchText: 'what are birds?'
      }
    })
    const earlierSearchSucceeded = glossaryReducer(earlySearchSucceeded, {
      type: GLOSSARY_TERMS_FAILURE,
      meta: {
        timestamp: earlier
      }
    })

    expect(earlySearchSucceeded.resultsTimestamp).toEqual(late)
    expect(earlySearchSucceeded.results.get('what are birds?'))
      .toEqual(['recent result'])
    expect(earlierSearchSucceeded.resultsTimestamp).toEqual(late)
    expect(earlierSearchSucceeded.results.get('what are birds?'))
      .toEqual(['recent result'])
  })

  it('can set the detail index', () => {
    const initial = glossaryReducer(undefined, { type: 'any' })
    const withIndex = glossaryReducer(initial, {
      type: SET_GLOSSARY_DETAILS_INDEX,
      payload: 5
    })
    expect(initial.details.resultIndex).toEqual(0)
    expect(withIndex.details.resultIndex).toEqual(5)
  })

  it('can show and hide details', () => {
    const initial = glossaryReducer(undefined, { type: 'any' })
    const shown = glossaryReducer(initial, {
      type: SHOW_GLOSSARY_DETAILS,
      payload: true
    })
    const hidden = glossaryReducer(shown, {
      type: SHOW_GLOSSARY_DETAILS,
      payload: false
    })
    expect(initial.details.show).toEqual(false)
    expect(shown.details.show).toEqual(true)
    expect(hidden.details.show).toEqual(false)
  })
})
