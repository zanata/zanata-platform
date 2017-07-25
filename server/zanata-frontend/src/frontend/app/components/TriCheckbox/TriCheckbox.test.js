/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
import TriCheckbox from '.'

const callback = function (e) {}

describe('TriCheckbox', () => {
  it('can render !checked && !indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate={false} onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked={false} onChange={callback} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render !checked && indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked={false} onChange={callback} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render checked && !indeterminate markup', () => {
    const callback = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate={false} onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked onChange={callback} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can render checked && indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <input type="checkbox" className="tri-checkbox"
          checked onChange={callback} />
      </div>
    )
    expect(actual).toEqual(expected)
  })
  it('can handle click from checked && !indeterminate state', () => {
    const slothCheckbox = TestUtils.renderIntoDocument(
      <TriCheckbox
        checked
        indeterminate={false}
        onChange={callback} />
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
