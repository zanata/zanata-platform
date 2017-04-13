import React from 'react'
import Icons from '../app/components/Icons'
import { addLocaleData, IntlProvider } from 'react-intl'
import { locale, formats } from '../app/editor/config/intl'
import { addDecorator, configure } from '@kadira/storybook'
import './storybook.css'

// As long as we use any zanata-ui components, we should keep this import
import 'zanata-ui/dist/zanata-ui.css'

// fonts are included in index.html for the app, but storybook does not use that
var fontLink = document.createElement('link')
fontLink.setAttribute('href', 'http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,400italic') // eslint-disable-line max-len
fontLink.setAttribute('rel', 'stylesheet')
fontLink.setAttribute('type', 'text/css')
fontLink.setAttribute('async', '')
document.getElementsByTagName('head')[0].appendChild(fontLink)

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
  require('../app/editor/components/stories.js')
}

configure(loadStories, module)
