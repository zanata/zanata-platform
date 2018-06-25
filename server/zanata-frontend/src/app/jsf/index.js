// @ts-nocheck
/**
 * This is to add React components to existing jsf page besides sidebar/legacy.
 * Created by pahuang on 6/23/17.
 */
import React from 'react'
import { render } from 'react-dom'
import { createStore, applyMiddleware, compose } from 'redux'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger'
import { history } from '../history'
import { syncHistoryWithStore } from 'react-router-redux'
import { apiMiddleware } from 'redux-api-middleware'
import JsfRoot from './JsfRoot'
import rootReducer from '../reducers'
import {
  toggleMTMergeModal,
  toggleTMMergeModal
} from '../actions/version-actions'
import {
  showExportTMXModal
} from '../actions/tmx-actions'

const DEV = process.env && process.env.NODE_ENV === 'development'

const logger = createLogger({
  // options
})

const middleware = [
  DEV && require('redux-immutable-state-invariant').default(),
  thunk,
  apiMiddleware,
  // routerMiddleware,
  DEV && logger // must be last to avoid logging thunk/promise
].filter(Boolean)

const finalCreateStore = compose(
  applyMiddleware(...middleware)
)(createStore)

// Call and assign the store with no initial state
const store = ((initialState) => {
  const store = finalCreateStore(rootReducer, initialState)
  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('../reducers', () => {
      const nextRootReducer = require('../reducers')
      store.replaceReducer(nextRootReducer)
    })
  }
  return store
})()

const enhancedHistory = syncHistoryWithStore(history, store)

export default function mountReactToJsf () {
  // Attaching to window object so modals can be triggered from the JSF page
  window.toggleMTMergeModal = () => store.dispatch(toggleMTMergeModal())
  window.toggleTMMergeModal = () => store.dispatch(toggleTMMergeModal())
  window.toggleTMXExportModal = (show) =>
    store.dispatch(showExportTMXModal(show))
  const mountPoint = document.getElementById('jsfReactRoot')

  render(<JsfRoot store={store} history={enhancedHistory} />, mountPoint)
}
