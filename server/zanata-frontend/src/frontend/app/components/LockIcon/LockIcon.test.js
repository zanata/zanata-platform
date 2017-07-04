jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import LockIcon from '.'

describe('LockIconTest', () => {
  it('renders a LockIcon when given a READONLY status', () => {
    const svgIcon = `<use xlink:href="#Icon-locked" />`
    const actual = ReactDOMServer.renderToStaticMarkup(
      <LockIcon status={'READONLY'} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className="s0 icon-locked">
        <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
          style={{ fill: 'currentColor' }} />
      </span>
    )
    expect(actual).toEqual(expected)
  })
  it('renders an empty span when given an ACTIVE status', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <LockIcon status={'ACTIVE'} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span></span>
    )
    expect(actual).toEqual(expected)
  })
})
