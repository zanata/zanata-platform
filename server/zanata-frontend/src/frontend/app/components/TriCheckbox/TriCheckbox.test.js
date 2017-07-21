/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
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
  it('can handle click from checked && !indeterminate state', () => {
    const clickFun = function (e) {}
    const slothCheckbox = TestUtils.renderIntoDocument(
      <TriCheckbox
        checked
        indeterminate={false}
        onChange={clickFun} />
    )
    try {
      TestUtils.Simulate.click(ReactDOM.findDOMNode(slothCheckbox))
    } catch (e) {
      // swallow on purpose, valid for code to not bind onClick
    }
    expect(slothCheckbox.props.checked).toEqual(true)
    expect(slothCheckbox.props.indeterminate).toEqual(false)
  })
})
