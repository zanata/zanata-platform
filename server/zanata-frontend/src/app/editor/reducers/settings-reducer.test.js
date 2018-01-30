/* global jest describe it expect */

import reducer, {
  ENTER_SAVES_IMMEDIATELY,
  KEY_SUGGESTIONS_VISIBLE,
  SYNTAX_HIGHLIGTING,
  SUGGESTIONS_DIFF,
  getSuggestionsPanelVisible
} from './settings-reducer'
import { createAction } from 'redux-actions'
import {
  SETTINGS_REQUEST,
  SETTINGS_SUCCESS,
  SETTINGS_FAILURE,
  SETTING_UPDATE,
  SETTINGS_SAVE_REQUEST,
  SETTINGS_SAVE_SUCCESS,
  SETTINGS_SAVE_FAILURE
} from '../actions/settings-action-types'

describe('settings-reducer test', () => {
  it('generates initial state', () => {
    const initial = reducer(undefined, createAction('any')())
    expect(initial).toEqual({
      fetching: false,
      error: undefined,
      settings: {
        [ENTER_SAVES_IMMEDIATELY]: {
          value: false,
          saving: false,
          error: undefined
        },
        [KEY_SUGGESTIONS_VISIBLE]: {
          value: true,
          saving: false,
          error: undefined
        },
        [SYNTAX_HIGHLIGTING]: {
          value: false,
          saving: false,
          error: undefined
        },
        [SUGGESTIONS_DIFF]: {
          value: true,
          saving: false,
          error: undefined
        }
      }
    })
  })
  // FIXME check the loaded settings individually, don't depend on initial state
  it('can load settings', () => {
    const loading = reducer(undefined, createAction(SETTINGS_REQUEST)())
    const loaded = reducer(loading, createAction(SETTINGS_SUCCESS)({
      [KEY_SUGGESTIONS_VISIBLE]: 'false',
      foo: 'bar',
      unknown: 'false'
    }))
    expect(loading.fetching).toBe(true)
    expect(loaded).toEqual({
      fetching: false,
      error: undefined,
      settings: {
        [ENTER_SAVES_IMMEDIATELY]: {
          value: false,
          saving: false,
          error: undefined
        },
        [KEY_SUGGESTIONS_VISIBLE]: {
          value: false,
          saving: false,
          error: undefined
        },
        [SYNTAX_HIGHLIGTING]: {
          value: false,
          saving: false,
          error: undefined
        },
        [SUGGESTIONS_DIFF]: {
          value: true,
          saving: false,
          error: undefined
        },
        foo: {
          value: 'bar',
          saving: false,
          error: undefined
        },
        unknown: {
          // unknown settings should not be parsed, even if they can be
          value: 'false',
          saving: false,
          error: undefined
        }
      }
    })
  })
  it('falls back on string if value parsing fails', () => {
    console.error = jest.fn()
    const loaded = reducer(undefined, createAction(SETTINGS_SUCCESS)({
      [KEY_SUGGESTIONS_VISIBLE]: 'flalse'
    }))
    expect(loaded.settings[KEY_SUGGESTIONS_VISIBLE].value).toBe('flalse')
    expect(console.error).toBeCalled()
  })
  it('getSuggestionsPanelVisible selects correct state', () => {
    const initial = reducer(undefined, createAction('any'))
    const updated = reducer(initial,
      createAction(SETTING_UPDATE)({[KEY_SUGGESTIONS_VISIBLE]: false}))
    expect(getSuggestionsPanelVisible(initial)).toBe(true)
    expect(getSuggestionsPanelVisible(updated)).toBe(false)
  })
  it('can record failed settings load', () => {
    const loading = reducer(undefined, createAction(SETTINGS_REQUEST)())
    const error = new Error('it broke')
    const failed = reducer(loading,
      createAction(SETTINGS_FAILURE)(error))
    expect(loading.fetching).toBe(true)
    expect(failed.fetching).toBe(false)
    expect(failed.error).toEqual(error)
  })
  it('can update setting', () => {
    const updated = reducer(undefined,
      createAction(SETTING_UPDATE)({[KEY_SUGGESTIONS_VISIBLE]: false}))
    expect(updated.settings[KEY_SUGGESTIONS_VISIBLE].value).toBe(false)
  })
  it('fails setting update when there are the wrong number of keys', () => {
    const initial = reducer(undefined, createAction('any')())
    console.error = jest.fn()
    const notUpdated = reducer(undefined,
      createAction(SETTING_UPDATE)({
        [KEY_SUGGESTIONS_VISIBLE]: false,
        'something-else': true
      }))
    expect(notUpdated).toEqual(initial)
    expect(console.error).toHaveBeenCalledWith(
      'expected object with exactly 1 key, but got: {"suggestions-visible":false,"something-else":true}') // eslint-disable-line max-len
  })
  it('can update setting that is missing default', () => {
    console.error = jest.fn()
    const updated = reducer(undefined,
      createAction(SETTING_UPDATE)({'puppy-type': 'Beagle'}))
    expect(updated.settings['puppy-type'].value).toBe('Beagle')
    expect(console.error).toHaveBeenCalledWith(
      'updating puppy-type, but it is not in state to update')
  })
  it('can record setting save request', () => {
    const state = reducer(undefined, { type: SETTINGS_SAVE_REQUEST, meta: {
      settings: { [KEY_SUGGESTIONS_VISIBLE]: false }
    }})
    expect(state.settings[KEY_SUGGESTIONS_VISIBLE].saving).toBe(true)
  })
  it('can record setting save success', () => {
    const state = reducer(undefined, { type: SETTINGS_SAVE_SUCCESS, meta: {
      settings: { [KEY_SUGGESTIONS_VISIBLE]: false }
    }})
    expect(state.settings[KEY_SUGGESTIONS_VISIBLE].saving).toBe(false)
    expect(state.settings[KEY_SUGGESTIONS_VISIBLE].error).toBeUndefined()
  })
  it('can record setting save error', () => {
    const error = new Error('it broke')
    const state = reducer(undefined, { type: SETTINGS_SAVE_FAILURE,
      payload: error,
      meta: {
        settings: { [KEY_SUGGESTIONS_VISIBLE]: false }
      }})
    expect(state.settings[KEY_SUGGESTIONS_VISIBLE].saving).toBe(false)
    expect(state.settings[KEY_SUGGESTIONS_VISIBLE].error).toEqual(error)
  })
})
