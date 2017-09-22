/* global jest describe it expect */
jest.disableAutomock()

import contextReducer from './context-reducer'
import { ROUTING_PARAMS_CHANGED } from '../actions/action-types'
import { DEFAULT_LOCALE } from './ui-reducer'

describe('context-reducer test', () => {
  it('generates initial state', () => {
    const initial = contextReducer(undefined, { type: 'an action' })
    expect(initial).toEqual({
      sourceLocale: DEFAULT_LOCALE
    })
  })

  it('merges in new routing params', () => {
    const initial = {
      sourceLocale: DEFAULT_LOCALE,
      a: 'a',
      b: 'b',
      c: 'c'
    }
    const updated = contextReducer(initial, {
      type: ROUTING_PARAMS_CHANGED,
      // Note: the reducer does not check for valid internal structure of params
      payload: {
        a: 'a',
        b: 'B',
        d: 'd'
      }
    })
    expect(updated).toEqual({
      sourceLocale: DEFAULT_LOCALE,
      a: 'a',
      b: 'B',
      c: 'c',
      d: 'd'
    })
  })
})
