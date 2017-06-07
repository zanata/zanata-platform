/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import COMPONENT_NAME_HERE from '.'

describe('COMPONENT_NAME_HERE Test', () => {
  it('can render markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <COMPONENT_NAME_HERE
        fancy={false}
        onClick={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>TODO write a test for markup.</div>
    )
    expect(actual).toEqual(expected)
  })

  it('can render fancy markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <COMPONENT_NAME_HERE
        fancy
        onClick={clickFun} />)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>TODO write a test for markup.</div>
    )
    expect(actual).toEqual(expected)
  })

  it('can be clicked', () => {
    let clickEvent = 'not called'
    const clickFun = function (e) {
      clickEvent = e.value
    }

    const component = TestUtils.renderIntoDocument(
      <COMPONENT_NAME_HERE fancy onClick={clickFun} />
    )
    // simulate click event
    TestUtils.Simulate.click(
      ReactDOM.findDOMNode(component).querySelector('button'),
      {value: 'clicked'})
    expect(clickEvent).toEqual('clicked',
      'COMPONENT_NAME_HERE click event should be fired')
  })
})
