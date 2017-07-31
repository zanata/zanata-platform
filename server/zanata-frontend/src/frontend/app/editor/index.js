import 'babel-polyfill'
import React from 'react'
import ReactDOM from 'react-dom'
import { baseUrl } from './config'
import { locale, formats } from './config/intl'
import { addLocaleData, IntlProvider } from 'react-intl'
import { createStore, applyMiddleware } from 'redux'
import { apiMiddleware } from 'redux-api-middleware'
import { Provider } from 'react-redux'
import { browserHistory, Router, Route } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'
import newContextFetchMiddleware from './middlewares/new-context-fetch'
import searchSelectedPhraseMiddleware
  from './middlewares/selected-phrase-searches'
import getStateInActions from './middlewares/getstate-in-actions'
import titleUpdateMiddleware from './middlewares/title-update'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger'
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

  // example uses createHistory, but the latest bundles history with
  // react-router and has some defaults, so now I am just using one of those.
  // const history = createHistory()
  const history = browserHistory
  history.basename = baseUrl

  const loggerMiddleware = createLogger({
    predicate: (getState, action) =>
      process.env && (process.env.NODE_ENV === 'development'),
    actionTransformer: (action) => {
      if (typeof action.type !== 'symbol') {
        console.warn('You should use a Symbol for this action type: ' +
          String(action.type))
      }
      return {
        ...action,
        // allow symbol action type to be printed properly in logs
        type: String(action.type)
      }
    }
  })

  // const reduxRouterMiddleware = syncHistory(history)
  const createStoreWithMiddleware =
    applyMiddleware(
      titleUpdateMiddleware,
      newContextFetchMiddleware,
      searchSelectedPhraseMiddleware,
      // reduxRouterMiddleware,
      thunk,
      apiMiddleware,
      // must run after thunk because it fails with thunks
      getStateInActions,
      loggerMiddleware
    )(createStore)

  const store = createStoreWithMiddleware(rootReducer)
  // reduxRouterMiddleware.listenForReplays(store)

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
  // FIXME must test this, may require an additional polyfill to be available
  // eslint-disable-next-line max-len
  // see https://webpack.js.org/guides/migrating/#require-ensure-and-amd-require-are-asynchronous
  require.ensure([], (require) => {
    // This is 'require' on purpose, do not change to 'import'
    require('intl')
    runApp()
  }, 'intl-polyfill')
}
