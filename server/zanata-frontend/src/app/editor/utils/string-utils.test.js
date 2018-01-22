/* global jest describe expect it */

import {
  startsWith,
  endsWith,
  equals,
  replaceRange,
  oneLiner
} from './string-utils'

describe('status-util', () => {
  it('startsWith(...) works as expected', () => {
    expect(startsWith('hello', 'hell')).toEqual(true)
    expect(startsWith('hello', 'Hell')).toEqual(false)
    expect(startsWith('hello', 'Hell', false)).toEqual(false)
    expect(startsWith('hello', 'Hell', true)).toEqual(true)
  })

  it('endsWith(...) works as expected', () => {
    expect(endsWith('great', 'eat')).toEqual(true)
    expect(endsWith('great', 'Eat')).toEqual(false)
    expect(endsWith('great', 'Eat', false)).toEqual(false)
    expect(endsWith('great', 'Eat', true)).toEqual(true)
  })

  it('equals(...) works as expected', () => {
    expect(equals('hat', 'hat')).toEqual(true)
    expect(equals('hat', 'Hat')).toEqual(false)
    expect(equals('hat', 'Hat', false)).toEqual(false)
    expect(equals('hat', 'Hat', true)).toEqual(true)
  })

  it('replaceRange(...) works as expected', () => {
    expect(replaceRange('Hello, John', 'nes', 9, 11))
      .toEqual('Hello, Jones')
    expect(replaceRange('What a great hat!', 'E', 0, 2))
      .toEqual('Eat a great hat!')
  })

  it('oneLiner(...) works as expected', () => {
    const foo = 'delicious'
    const bar = 'broccoli'
    expect(oneLiner`That was a totally
      ${foo} stack of ${bar}
      I just ate.`)
      .toEqual('That was a totally delicious stack of broccoli I just ate.')
  })
})
