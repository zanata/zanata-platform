/* global describe expect it */
// @ts-nocheck
import React from 'react'
import * as ReactDOM from 'react-dom'
import * as ReactDOMServer from 'react-dom/server'
import * as TestUtils from 'react-dom/test-utils'
import { Icon } from '../../../components'
import IconButton from '.'
import Button from 'antd/lib/button'

describe('IconButtonTest', () => {

  it('IconButton click event', () => {
    let clickEvent = 'freshing'
    const clickFun = function (e) {
      clickEvent = e.value
    }

    const refreshButton = TestUtils.renderIntoDocument(
      <IconButton
        icon="iced-tea"
        title="Iced Tea"
        onClick={clickFun} />
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
        disabled
        icon="iced-tea"
        title="Iced Tea"
        onClick={clickFun} />
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
