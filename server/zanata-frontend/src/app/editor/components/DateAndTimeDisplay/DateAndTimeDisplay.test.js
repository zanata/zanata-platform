/* global jest describe it expect */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import DateAndTimeDisplay from '.'
import { addLocaleData, IntlProvider } from 'react-intl'
import { locale, formats } from '../../config/intl'
import Icon from '../../../components/Icon'

// Set up locale so formats will work properly
addLocaleData({
  locale: 'en-US'
})

describe('DateAndTimeDisplay Test', () => {
  it('can render markup', () => {
    const dateTime = new Date(1985, 9, 26, 1, 21)
    // <IntlProvider> is at the top level of the app
    const actual = ReactDOMServer.renderToStaticMarkup(
      <IntlProvider locale={locale} formats={formats}>
        <DateAndTimeDisplay dateTime={dateTime} className="marty" />
      </IntlProvider>)
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span className="marty">
        <Icon name="clock" className="n1" />&nbsp;
        <span>Oct 26, 1985</span>&nbsp;
        <span>1:21 AM</span>
      </span>
    )
    expect(actual).toEqual(expected)
  })
})
