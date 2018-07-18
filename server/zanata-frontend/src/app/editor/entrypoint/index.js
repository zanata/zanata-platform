// @ts-nocheck
import 'babel-polyfill'
import React from 'react'
import * as ReactDOM from 'react-dom'
import { appUrl, serverUrl } from '../../config'
import createStoreWithMiddleware from '../middlewares'
import { addLocaleData } from 'react-intl'
import { Provider } from 'react-redux'
import { browserHistory, Router, Route } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'
import rootReducer from '../reducers'
import addWatchers from '../watchers'
import { isEmpty } from 'lodash'
import { appLocale } from '../../config'
import Root from '../containers/Root'
import NeedSlugMessage from '../containers/NeedSlugMessage'
import { fetchSettings } from '../actions/settings-actions'

// Set the path that webpack will try to load extra chunks from
// This is needed to load intl-polyfill
__webpack_public_path__ = serverUrl || '/' // eslint-disable-line

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
  // Dynamically load the locale data of the selected appLocale
  try {
    addLocaleData(require(`react-intl/locale-data/${appLocale}`))
  } catch (e) {
    console.error(`Locale module not found for locale: ${appLocale}
    Defaulting to en`)
    addLocaleData(require('react-intl/locale-data/en'))
  }

  const history = browserHistory
  history.basename = appUrl
  const store = createStoreWithMiddleware(rootReducer)
  addWatchers(store)

  const enhancedHistory = syncHistoryWithStore(history, store)

  const rootElement = document.getElementById('appRoot')

  // FIXME current (old) behaviour when not enough params are specified is to
  //       reset to blank app and not even keep the project/version part of the
  //       URL. As soon as it has the /translate part of the URL it grabs the
  //       first doc and language in the list and goes ahead.
  //   Should be able to do better than that.

  // Load user settings once
  store.dispatch(fetchSettings())

  const route = '/project/translate/:projectSlug/v/:versionSlug/**'
  const path = !isEmpty(appUrl) ? appUrl + route : route

  // TODO when translations are available, load user locale translations with
  //   require.ensure and pass to IntlProvider as messages={...}
  // defaultLocale will use the default messages with no errors
  ReactDOM.render(
    <Provider store={store}>
      <Router history={enhancedHistory}>
        {/* The ** is docId, captured as params.splat by react-router. */}
        <Route path={path} component={Root} />
        <Route path="/*" component={NeedSlugMessage} />
      </Router>
    </Provider>, rootElement)
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
