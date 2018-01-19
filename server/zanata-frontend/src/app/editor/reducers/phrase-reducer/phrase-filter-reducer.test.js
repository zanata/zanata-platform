/* global jest describe it expect */

import {
  TOGGLE_ADVANCED_PHRASE_FILTERS,
  UPDATE_PHRASE_FILTER
} from '../../actions/phrases-action-types'
import { phraseFilterReducer } from './phrase-filter-reducer'
import { defaultState as statusDefaultState } from './filter-status-reducer'

describe('filter-status-reducer test', () => {
  it('can set default state', () => {
    const defaultState = phraseFilterReducer(undefined, { type: 'ANY' })
    expect(defaultState).toEqual({
      showAdvanced: false,
      advanced: {
        searchString: '',
        resId: '',
        lastModifiedByUser: '',
        changedBefore: '',
        changedAfter: '',
        sourceComment: '',
        transComment: '',
        msgContext: ''
      },
      status: statusDefaultState
    })
  })

  it('can can toggle advanced panel', () => {
    const defaultState = phraseFilterReducer(undefined, { type: 'ANY' })
    const toggledOn = phraseFilterReducer(defaultState, {
      type: TOGGLE_ADVANCED_PHRASE_FILTERS })
    const toggledOff = phraseFilterReducer(toggledOn, {
      type: TOGGLE_ADVANCED_PHRASE_FILTERS })

    expect(defaultState.showAdvanced).toEqual(false)
    expect(toggledOn.showAdvanced).toEqual(true)
    expect(toggledOff.showAdvanced).toEqual(false)
  })

  it('can update single search keys', () => {
    const withText = phraseFilterReducer(undefined, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        searchString: 'covfefe'
      }
    })
    const withSourceComment = phraseFilterReducer(withText, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        sourceComment: 'Has anyone really been far even as decided to use?'
      }
    })
    expect(withText.advanced).toEqual({
      searchString: 'covfefe',
      resId: '',
      lastModifiedByUser: '',
      changedBefore: '',
      changedAfter: '',
      sourceComment: '',
      transComment: '',
      msgContext: ''
    })
    expect(withSourceComment.advanced).toEqual({
      searchString: 'covfefe',
      resId: '',
      lastModifiedByUser: '',
      changedBefore: '',
      changedAfter: '',
      sourceComment: 'Has anyone really been far even as decided to use?',
      transComment: '',
      msgContext: ''
    })
  })

  it('can update multiple search keys', () => {
    const withLastModified = phraseFilterReducer(undefined, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        lastModifiedByUser: 'bulbasaur',
        changedBefore: '2017/06/07',
        changedAfter: '2017/01/01'
      }
    })
    const withOtherStuff = phraseFilterReducer(withLastModified, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        lastModifiedByUser: 'ivysaur',
        transComment: "Gotta catch 'em all"
      }
    })
    expect(withLastModified.advanced).toEqual({
      searchString: '',
      resId: '',
      lastModifiedByUser: 'bulbasaur',
      changedBefore: '2017/06/07',
      changedAfter: '2017/01/01',
      sourceComment: '',
      transComment: '',
      msgContext: ''
    })
    expect(withOtherStuff.advanced).toEqual({
      searchString: '',
      resId: '',
      lastModifiedByUser: 'ivysaur',
      changedBefore: '2017/06/07',
      changedAfter: '2017/01/01',
      sourceComment: '',
      transComment: "Gotta catch 'em all",
      msgContext: ''
    })
  })
})
