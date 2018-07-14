/* global jest describe it expect */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import LockIcon from '.'
import {Icon } from '../../components'
import Tooltip from "antd/lib/tooltip";

describe('LockIcon', () => {
  it('renders a LockIcon when given a READONLY status', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <LockIcon status={'READONLY'} />
    )
    const title = <span id="tooltipreadonly">Read only</span>
    const expected = ReactDOMServer.renderToStaticMarkup(
      <Tooltip placement="top" title={title}>
        <Icon name="locked" className="s0 txt-warn" />
      </Tooltip>
    )
    expect(actual).toEqual(expected)
  })
  it('renders an empty span when given an ACTIVE status', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <LockIcon status={'ACTIVE'} />
    )
    expect(actual).toEqual('')
  })
})
