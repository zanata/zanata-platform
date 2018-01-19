/* global jest describe it expect */

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-dom/test-utils'
import TriCheckbox from '.'

const callback = function () {}

const utilRender = function (checked, indeterminate, onChange) {
  const rendered = TestUtils.renderIntoDocument(<TriCheckbox
    checked={checked}
    indeterminate={indeterminate}
    onChange={onChange}
    />)
  return TestUtils.findRenderedDOMComponentWithTag(rendered, 'input')
}

describe('TriCheckbox', () => {
  it('can render !checked && !indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate={false} onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <input type="checkbox" className="triCheckbox"
        checked={false} onChange={callback} />
    )
    const utilRendered = utilRender(false, false, callback)
    expect(utilRendered.indeterminate).toEqual(false)
    expect(actual).toEqual(expected)
  })
  it('can render !checked && indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked={false} indeterminate onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <input type="checkbox" className="triCheckbox"
        checked={false} onChange={callback} />
    )
    const utilRendered = utilRender(false, true, callback)
    expect(utilRendered.indeterminate).toEqual(true)
    expect(actual).toEqual(expected)
  })
  it('can render checked && !indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate={false} onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <input type="checkbox" className="triCheckbox"
        checked onChange={callback} />
    )
    const utilRendered = utilRender(true, false, callback)
    expect(utilRendered.indeterminate).toEqual(false)
    expect(actual).toEqual(expected)
  })
  it('can render checked && indeterminate markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TriCheckbox checked indeterminate onChange={callback} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <input type="checkbox" className="triCheckbox"
        checked onChange={callback} />
    )
    const utilRendered = utilRender(true, true, callback)
    expect(utilRendered.indeterminate).toEqual(true)
    expect(actual).toEqual(expected)
  })
  it('can handle click from checked && !indeterminate state', () => {
    let changeEvent = 'do nothing'
    const livingItUp = function (e) {
      changeEvent = e.value
    }
    const slothCheckbox = TestUtils.renderIntoDocument(
      <TriCheckbox
        checked
        indeterminate={false}
        onChange={livingItUp}
        onClick={livingItUp} />
    )
    try {
      TestUtils.Simulate.click(ReactDOM.findDOMNode(slothCheckbox),
        {value: 'sleep'})
    } catch (e) {
      // swallow on purpose, valid for code to not bind onClick
    }
    expect(changeEvent).toEqual('sleep')
  })
})
