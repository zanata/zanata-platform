/* global document */
import * as React from 'react'
import Icons from '../app/components/Icons'
import { addLocaleData, IntlProvider } from 'react-intl'
import { locale, formats } from '../app/editor/config/intl'
import { addDecorator, configure } from '@storybook/react'
import './storybook.less'

// Storyshots test runs this file too, with no document available.
if (typeof document !== 'undefined') {
  // fonts are included in index.html for the app, but storybook does not use that
  var fontLink = document.createElement('link')
  fontLink.setAttribute('href', 'https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css') // eslint-disable-line max-len
  fontLink.setAttribute('rel', 'stylesheet')
  fontLink.setAttribute('type', 'text/css')
  fontLink.setAttribute('async', '')
  document.getElementsByTagName('head')[0].appendChild(fontLink)

  var lessLink = document.createElement('link')
  lessLink.setAttribute('href', '//cdnjs.cloudflare.com/ajax/libs/less.js/2.5.1/less.min.js') // eslint-disable-line max-len
  lessLink.setAttribute('rel', 'stylesheet')
  lessLink.setAttribute('type', 'text/css')
  lessLink.setAttribute('async', '')
  document.getElementsByTagName('head')[0].appendChild(lessLink)
}

// Set up locale data so formats etc. will work properly
addLocaleData({
  locale: 'en-US'
})

/*
 * This sets up the context that all the components are expecting to be in.
 *
 * - All components can expect to have react-intl
 *   configured through <IntlProvider/>
 * - All components can expect the icon svg to be included by <Icons />
 * - All components can expect Source Sans Pro to be available in weights
 *   300, 400 (plain and italic), 600 and 700
 */
addDecorator((story) => (
  <IntlProvider locale={locale} formats={formats}>
    <div style={{padding: '2em'}}>
      <Icons />
      {story()}
    </div>
  </IntlProvider>
))

function loadStories () {
  require('../app/components/components.story.js')
}

configure(loadStories, module)
