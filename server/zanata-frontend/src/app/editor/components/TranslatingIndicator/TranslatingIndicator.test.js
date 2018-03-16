import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import TranslatingIndicator from '.'
import { Icon } from '../../../components'
import { Row } from 'react-bootstrap'
import { IntlProvider } from 'react-intl'

/* global describe expect it */
describe('TranslatingIndicator', () => {
  it('renders default viewing markup', () => {
    const permissions = {
      reviewer: false,
      translator: false
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale='en'>
        <TranslatingIndicator permissions={permissions} />
      </IntlProvider>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      /* eslint-disable max-len */
      <button className='Link--neutral u-sPV-1-6 u-floatLeft u-sMR-1-4'>
        <Row>
          <Icon name='locked' className='s2 u-textDanger' /> <span
            className='u-ltemd-hidden TransIndicator u-sMR-1-4'>
            <span>Viewing</span>
          </span>
        </Row>
      </button>
      /* eslint-enable max-len */
    )
    expect(actual).toEqual(expected)
  })
  it('renders reviewer markup', () => {
    const permissions = {
      reviewer: true,
      translator: false
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale='en'>
        <TranslatingIndicator permissions={permissions} />
      </IntlProvider>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      /* eslint-disable max-len */
      <button className='Link--neutral u-sPV-1-6 u-floatLeft u-sMR-1-4'>
        <Row>
          <Icon name='review' className='s2' /> <span
            className='u-ltemd-hidden TransIndicator u-sMR-1-4'>
            <span>Reviewing</span>
          </span>
        </Row>
      </button>
      /* eslint-enable max-len */
    )
    expect(actual).toEqual(expected)
  })
  it('renders translator markup', () => {
    const permissions = {
      reviewer: false,
      translator: true
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale='en'>
        <TranslatingIndicator permissions={permissions} />
      </IntlProvider>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      /* eslint-disable max-len */
      <button className='Link--neutral u-sPV-1-6 u-floatLeft u-sMR-1-4'>
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
