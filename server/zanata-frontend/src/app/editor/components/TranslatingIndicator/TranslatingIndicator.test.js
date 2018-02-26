import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import TranslatingIndicator from '.'
import { Icon } from '../../../components'
import { Row } from 'react-bootstrap'
import { IntlProvider } from 'react-intl'

/* global describe expect it */
describe('TranslatingIndicator', () => {
  it('renders default markup', () => {
    const gettextCatalog = {
      getString: (key) => {
        return key
      }
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale='en'>
        <TranslatingIndicator gettextCatalog={gettextCatalog} />
      </IntlProvider>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      /* eslint-disable max-len */
      <button className='Link--neutral u-sPV-1-6 u-floatLeft u-sizeHeight-1_1-2 u-sMR-1-4'>
        <Row>
          <Icon name='translate' className='s2' /> <span
            className='u-ltemd-hidden TransIndicator u-sMR-1-4'>
            <span>Translating</span>
          </span>
        </Row>
      </button>
      /* eslint-enable max-len */
    )
    expect(actual).toEqual(expected)
  })
})
