import 'babel-polyfill'
import React from 'react'
import { render } from 'react-dom'
import { mapValues } from 'lodash'
import { createStore, applyMiddleware, compose } from 'redux'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger'
import { browserHistory } from 'react-router'
import { syncHistory } from 'react-router-redux'
import WebFont from 'webfontloader'
import { apiMiddleware } from 'redux-api-middleware'
import rootReducer from './reducers'
import Root from './containers/Root'
import { isJsonString } from './utils/StringUtils'
import './styles/atomic.css'
import './styles/style.less'

WebFont.load({
  google: {
    families: [
      'Source Sans Pro:200,400,600',
      'Source Code Pro:400,600'
    ]
  },
  timeout: 2000
})

// DONE change to browserHistory
// TODO set basename for the history to the serving location
//      (this should just be the / or /zanata/ distinction, since this app will
//       be served under paths like /glossary/* and /profile/* etc.)
// TODO make sure /profile/view/{username} routes to the right place
// TODO make sure Profile link on left points to /profile/view/{username}
// TODO change the default route to go to /profile/view/{username} instead of
// /profile/{username}
// TODO make all the other hash history links go to the non-hash places
//      this includes rewriting to the app URL, and the client-side part

const routerMiddleware = syncHistory(browserHistory)

const logger = createLogger({
  predicate: (getState, action) =>
  process.env && (process.env.NODE_ENV === 'development')
})

const finalCreateStore = compose(
  applyMiddleware(
    thunk,
    apiMiddleware,
    routerMiddleware,
    logger
  )
)(createStore)

// Call and assign the store with no initial state
const store = ((initialState) => {
  const store = finalCreateStore(rootReducer, initialState)
  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('./reducers', () => {
      const nextRootReducer = require('./reducers')
      store.replaceReducer(nextRootReducer)
    })
  }
  return store
})()

window.config = mapValues(window.config, (value) =>
  isJsonString(value) ? JSON.parse(value) : value)
// baseUrl should be /zanata or ''
window.config.baseUrl = window.config.baseUrl || ''

render(
  <Root store={store} history={browserHistory} />,
  document.getElementById('root')
)
