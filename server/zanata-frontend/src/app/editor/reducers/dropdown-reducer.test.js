/* global jest describe it expect */

import dropdownReducer from './dropdown-reducer'
import {
  OPEN_DROPDOWN, CLOSE_DROPDOWN, TOGGLE_DROPDOWN
} from '../actions/action-types'

describe('dropdown-reducer test', () => {
  it('generates initial state', () => {
    const initial = dropdownReducer(undefined, { type: 'an action' })
    expect(initial).toEqual(expect.objectContaining({
      openDropdownKey: undefined
    }))
    // must be defined keys
    expect(initial.docsKey).toBeDefined()
    expect(initial.localeKey).toBeDefined()
    expect(initial.uiLocaleKey).toBeDefined()

    // keys must be different
    expect(initial.docsKey).not.toEqual(initial.localeKey)
    expect(initial.localeKey).not.toEqual(initial.uiLocaleKey)
  })

  it('can open dropdowns', () => {
    const initial = dropdownReducer(undefined, { type: 'any' })
    const localeOpen = dropdownReducer(initial, {
      type: OPEN_DROPDOWN,
      payload: initial.localeKey
    })
    expect(localeOpen).toEqual(expect.objectContaining({
      openDropdownKey: initial.localeKey
    }))

    expect(dropdownReducer(localeOpen, {
      type: OPEN_DROPDOWN,
      payload: initial.docsKey
    })).toEqual(expect.objectContaining({
      openDropdownKey: initial.docsKey
    }))
  })

  it('can close dropdown', () => {
    const initial = dropdownReducer(undefined, { type: 'any' })
    const localeOpen = dropdownReducer(initial, {
      type: OPEN_DROPDOWN,
      payload: initial.localeKey
    })
    expect(dropdownReducer(localeOpen, {
      type: CLOSE_DROPDOWN
    })).toEqual(expect.objectContaining({
      openDropdownKey: undefined
    }))
  })

  it('can toggle dropdown', () => {
    const initial = dropdownReducer(undefined, { type: 'any' })
    const toggleAction = {
      type: TOGGLE_DROPDOWN,
      payload: initial.localeKey
    }
    const localeOpen = dropdownReducer(initial, toggleAction)
    expect(localeOpen).toEqual(expect.objectContaining({
      openDropdownKey: initial.localeKey
    }))
    const localeClosed = dropdownReducer(localeOpen, toggleAction)
    expect(localeClosed).toEqual(expect.objectContaining({
      openDropdownKey: undefined
    }))
  })

  it('leaves state unchanged for unknown actions', () => {
    const state = { a: 'a' }
    expect(dropdownReducer(state, { type: 'UNKNOWN' })).toEqual(state)
  })

  it('leaves state unchanged for unknown actions', () => {
    const state = { a: 'a' }
    expect(dropdownReducer(state, { type: 'UNKNOWN' })).toEqual(state)
  })
})
