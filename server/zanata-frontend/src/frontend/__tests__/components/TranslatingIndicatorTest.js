jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TranslatingIndicator from '../../app/editor/components/TranslatingIndicator'
import { Icon, Row } from 'zanata-ui'

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
        <Row>
          <Icon name="translate" size="2"/> <span
          className="u-ltemd-hidden u-sMR-1-4">
          Translating
          </span>
        </Row>
      </button>
    )
    expect(actual).toEqual(expected)
  })
})
