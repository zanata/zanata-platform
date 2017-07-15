import 'babel-polyfill'
import React from 'react'
import ReactDOM from 'react-dom'
import { baseUrl } from './config'
import { locale, formats } from './config/intl'
import createStoreWithMiddleware from './setup-middlewares'
import { addLocaleData, IntlProvider } from 'react-intl'
import { Provider } from 'react-redux'
import { browserHistory, Router, Route } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'
import rootReducer from './reducers'

import Root from './containers/Root'
import NeedSlugMessage from './containers/NeedSlugMessage'

// Set the path that webpack will try to load extra chunks from
// This is needed to load intl-polyfill
__webpack_public_path__ = baseUrl || '/' // eslint-disable-line

import './index.css'

/**
 * Top level of the Zanata editor app.
 *
 * This is the entry-point for the editor webpack build.
 *
 * Responsible for:-
 *  - creating the redux store
 *  - binding the redux store to a React component tree
 *  - rendering the React component tree to the page
 *  - ensuring the required css for the React component tree is available
 */
function runApp () {
  // TODO add all the relevant locale data
  // Something like:
  //  import en from './react-intl/locale-data/en'
  //  import de from './react-intl/locale-data/de'
  //    ... then just addLocaleData(en) etc.
  // See https://github.com/yahoo/react-intl/blob/master/UPGRADE.md
  // if ('ReactIntlLocaleData' in window) {
  //   Object.keys(window.ReactIntlLocaleData).forEach(lang => {
  //     addLocaleData(window.ReactIntlLocaleData[lang])
  //   })
  // }
  addLocaleData({
    locale: 'en-US'
  })

  const history = browserHistory
  history.basename = baseUrl
  const store = createStoreWithMiddleware(rootReducer)
  const enhancedHistory = syncHistoryWithStore(history, store)

  const rootElement = document.getElementById('appRoot')

  // FIXME current (old) behaviour when not enough params are specified is to
  //       reset to blank app and not even keep the project/version part of the
  //       URL. As soon as it has the /translate part of the URL it grabs the
  //       first doc and language in the list and goes ahead.
  //   Should be able to do better than that.

  ReactDOM.render(
    <IntlProvider locale={locale} formats={formats}>
      <Provider store={store}>
        <Router history={enhancedHistory}>
          {/* The ** is docId, captured as params.splat by react-router. */}
          <Route
            path="/project/translate/:projectSlug/v/:versionSlug/**"
            component={Root} />
          <Route path="/*" component={NeedSlugMessage} />
        </Router>
      </Provider>
    </IntlProvider>, rootElement)
}

if (window.Intl) {
  runApp()
} else {
  // Intl not present, so polyfill it.
  require.ensure([], (require) => {
    // This is 'require' on purpose, do not change to 'import'
    require('intl')
    runApp()
  }, 'intl-polyfill')
}
