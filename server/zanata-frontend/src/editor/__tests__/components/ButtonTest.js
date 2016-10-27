jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import Button from '../../app/components/Button'

describe('ButtonTest', () => {
  it('Button markup', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <Button
        title="Come on! Do it! Do it!"
        onClick={clickFun}
        className="im here">
        Come on!
      </Button>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <button
        className="im here"
        disabled={false}
        onClick={clickFun}
        title="Come on! Do it! Do it!">
        Come on!
      </button>
    )
    expect(actual).toEqual(expected)
  })

  it('Button markup (disabled)', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <Button
        disabled={true}
        title="No such thing, ol' buddy"
        onClick={clickFun}
        className="one-way-ticket">
        Who&apos;s our backup?
      </Button>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <button
        className="one-way-ticket is-disabled"
        disabled={true}
        onClick={undefined}
        title="No such thing, ol' buddy">
        Who&apos;s our backup?
      </button>
    )
    expect(actual).toEqual(expected)
  })

  it('Button click event', () => {
    let clickEvent = 'nowhere'
    const getToTheChopper = (e) => {
      clickEvent = e.value
    }

    const escapeButton = TestUtils.renderIntoDocument(
      <Button
        onClick={getToTheChopper}>
        Run! Get to...
      </Button>
    )
    TestUtils.Simulate.click(
      ReactDOM.findDOMNode(escapeButton), {value: 'the chopper'})
    expect(clickEvent).toEqual('the chopper',
      'Button click event should fire with correct event payload')
  })

  it('Button does not fire click when disabled', () => {
    let clickEvent = 'nowhere'
    const getToTheChopper = function (e) {
      clickEvent = e.value
    }

    const escapeButton = TestUtils.renderIntoDocument(
      <Button
        disabled={true}
        onClick={getToTheChopper}>
        Run! Get to...
      </Button>
    )

    try {
      // simulate click event
      TestUtils.Simulate.click(
        ReactDOM.findDOMNode(escapeButton), {value: 'the chopper'})
    } catch (e) {
      // swallow on purpose, valid for code to not bind onClick
    }
    expect(clickEvent).toEqual('nowhere',
      'Button click event should not fire when props.disabled is true')
  })
})
