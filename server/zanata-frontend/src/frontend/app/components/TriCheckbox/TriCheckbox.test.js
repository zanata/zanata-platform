/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TriCheckbox from '.'

describe('TriCheckbox', () => {
  // Markup tests should match truth table in storybook
  it('can render !checked && !indeterminate markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate={false} onChange={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked={false} indeterminate={false} onChange={clickFun} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render !checked && indeterminate markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate onChange={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked={false} indeterminate onChange={clickFun} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render checked && !indeterminate markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate={false} onChange={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked indeterminate={false} onChange={clickFun} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render checked && indeterminate markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate onChange={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked indeterminate onChange={clickFun} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
