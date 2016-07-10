jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TranslatingIndicator from '../../app/components/TranslatingIndicator'
import Icon from '../../app/components/Icon'

describe('TranslatingIndicatorTest', () => {
  it('TranslatingIndicator markup', () => {
    const gettextCatalog = {
      getString: (key) => {
        return key
      }
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <TranslatingIndicator gettextCatalog={gettextCatalog}/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <button className="Link--neutral u-sPV-1-4 u-floatLeft
                       u-sizeHeight-1_1-2 u-sMR-1-4">
        <Icon name="translate"/> <span
        className="u-ltemd-hidden u-sMR-1-4">
        Translating
      </span>
      </button>
    )
    expect(actual).toEqual(expected)
  })
})
