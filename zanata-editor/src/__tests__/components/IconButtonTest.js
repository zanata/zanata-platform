jest.disableAutomock()

import React from 'react'
import ReactDOM from 'react-dom'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import Icon from '../../app/components/Icon'
import IconButton from '../../app/components/IconButton'

describe('IconButtonTest', () => {
  it('IconButton markup', () => {
    const clickFun = function (e) {}

    const actual = ReactDOMServer.renderToStaticMarkup(<IconButton
      icon="classical"
      title="Mozart"
      onClick={clickFun}
      iconClass="pop-icon"
      buttonClass="push-me"/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <button
        className="push-me"
        onClick={clickFun}
        title="Mozart">
        <Icon
          name="classical"
          title="Mozart"
          className="Icon--sm pop-icon"/>
      </button>
    )
    expect(actual).toEqual(expected)
  })

  it('IconButton markup (disabled)', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(<IconButton
      icon="tea"
      title="Tea"
      onClick={clickFun}
      iconClass="cultural-icon"
      buttonClass="drink-me"
      disabled={true}/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <button
        className="drink-me is-disabled"
        disabled="true"
        onClick={clickFun}
        title="Tea">
        <Icon
          name="tea"
          title="Tea"
          className="Icon--sm cultural-icon"/>
      </button>
    )
    expect(actual).toEqual(expected)
  })

  it('IconButton click event', () => {
    let clickEvent = 'freshing'
    const clickFun = function (e) {
      clickEvent = e.value
    }

    const refreshButton = TestUtils.renderIntoDocument(
      <IconButton
        icon="iced-tea"
        title="Iced Tea"
        onClick={clickFun}/>
    )
    // simulate click event
    TestUtils.Simulate.click(
      ReactDOM.findDOMNode(refreshButton), {value: 'refreshing'})
    expect(clickEvent).toEqual('refreshing',
      'IconButton click event should fire with correct event payload')
  })

  it('IconButton does not fire click when disabled', () => {
    let clickEvent = 'freshing'
    const clickFun = function (e) {
      clickEvent = e.value
    }

    const refreshButton = TestUtils.renderIntoDocument(
      <IconButton
        icon="iced-tea"
        title="Iced Tea"
        onClick={clickFun}
        disabled={true}/>
    )
    // throws if onClick is not bound
    try {
      // simulate click event
      TestUtils.Simulate.click(
        ReactDOM.findDOMNode(refreshButton), {value: 'refreshing'})
    } catch (e) {
      // swallow on purpose, valid for code to not bind onClick
    }

    expect(clickEvent).toEqual('freshing',
      'IconButton click event should not fire when props.disabled is true')
  })
})
