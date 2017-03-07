jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import SuggestionMatchPercent from '../../app/components/SuggestionMatchPercent'

describe('SuggestionMatchPercentTest', () => {
  it('SuggestionMatchPercent markup (imported)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="imported"
        percent={12.34567}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textSecondary">
        12.3%
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionMatchPercent markup (translated)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="translated"
        percent={46}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textSuccess">
        46%
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionMatchPercent markup (approved)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="approved"
        percent={67.89}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textHighlight">
        67.9%
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionMatchPercent markup (> 99%)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="approved"
        percent={99.956789}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textHighlight">
        99.96%
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionMatchPercent markup (99.99999 != 100)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="approved"
        percent={99.9999999}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textHighlight">
        99.99%
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionMatchPercent markup (100%)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionMatchPercent
        matchType="approved"
        percent={100}/>
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="u-textHighlight">
        100%
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
