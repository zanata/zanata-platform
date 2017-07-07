/* global jest describe it expect */
jest.disableAutomock()

import {
  TOGGLE_ADVANCED_PHRASE_FILTERS,
  UPDATE_PHRASE_FILTER
} from '../../actions/phrases-action-types'
import { phraseFilterReducer } from './phrase-filter-reducer'

describe('filter-status-reducer test', () => {
  it('can set default state', () => {
    const defaultState = phraseFilterReducer(undefined, { type: 'ANY' })
    expect(defaultState).toEqual({
      showAdvanced: false,
      advanced: {
        text: '',
        resourceId: '',
        lastModifiedBy: '',
        lastModifiedBefore: '',
        lastModifiedAfter: '',
        sourceComment: '',
        translationComment: '',
        msgctxt: ''
      }
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
        text: 'covfefe'
      }
    })
    const withSourceComment = phraseFilterReducer(withText, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        sourceComment: 'Has anyone really been far even as decided to use?'
      }
    })
    expect(withText.advanced).toEqual({
      text: 'covfefe',
      resourceId: '',
      lastModifiedBy: '',
      lastModifiedBefore: '',
      lastModifiedAfter: '',
      sourceComment: '',
      translationComment: '',
      msgctxt: ''
    })
    expect(withSourceComment.advanced).toEqual({
      text: 'covfefe',
      resourceId: '',
      lastModifiedBy: '',
      lastModifiedBefore: '',
      lastModifiedAfter: '',
      sourceComment: 'Has anyone really been far even as decided to use?',
      translationComment: '',
      msgctxt: ''
    })
  })

  it('can update multiple search keys', () => {
    const withLastModified = phraseFilterReducer(undefined, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        lastModifiedBy: 'bulbasaur',
        lastModifiedBefore: '2017/06/07',
        lastModifiedAfter: '2017/01/01'
      }
    })
    const withOtherStuff = phraseFilterReducer(withLastModified, {
      type: UPDATE_PHRASE_FILTER,
      payload: {
        lastModifiedBy: 'ivysaur',
        translationComment: "Gotta catch 'em all"
      }
    })
    expect(withLastModified.advanced).toEqual({
      text: '',
      resourceId: '',
      lastModifiedBy: 'bulbasaur',
      lastModifiedBefore: '2017/06/07',
      lastModifiedAfter: '2017/01/01',
      sourceComment: '',
      translationComment: '',
      msgctxt: ''
    })
    expect(withOtherStuff.advanced).toEqual({
      text: '',
      resourceId: '',
      lastModifiedBy: 'ivysaur',
      lastModifiedBefore: '2017/06/07',
      lastModifiedAfter: '2017/01/01',
      sourceComment: '',
      translationComment: "Gotta catch 'em all",
      msgctxt: ''
    })
  })
})
