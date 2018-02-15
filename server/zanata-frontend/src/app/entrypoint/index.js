// @ts-nocheck
import 'babel-polyfill'
import 'es6-symbol/implement'
import React from 'react'
import { render } from 'react-dom'
import { createStore, applyMiddleware, compose } from 'redux'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger'
import { history } from '../history'
import { syncHistoryWithStore } from 'react-router-redux'
import * as WebFont from 'webfontloader'
import { apiMiddleware } from 'redux-api-middleware'
import rootReducer from '../reducers'
import Root from '../containers/Root'

import '../styles/style.less'
import '../editor/index.css'

WebFont.load({
  google: {
    families: [
      'Source Sans Pro:200,400,600',
      'Source Code Pro:400,600'
    ]
  },
  timeout: 2000
})

// const routerMiddleware = syncHistory(history)

const logger = createLogger({
  predicate: (_getState, _action) =>
  process.env && (process.env.NODE_ENV === 'development')
})

const finalCreateStore = compose(
  applyMiddleware(
    thunk,
    apiMiddleware,
    // routerMiddleware,
    logger
  )
)(createStore)

// Call and assign the store with no initial state
const store = ((initialState) => {
  const store = finalCreateStore(rootReducer, initialState)
  // @ts-ignore module.hot
  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    // @ts-ignore module.hot
    module.hot.accept('../reducers', () => {
      const nextRootReducer =
        /** @type {any} */ (require('../reducers'))
      store.replaceReducer(nextRootReducer)
    })
  }
  return store
})()

const enhancedHistory = syncHistoryWithStore(history, store)

render(
  // @ts-ignore store
  <Root store={store} history={enhancedHistory} />,
  document.getElementById('root')
)
