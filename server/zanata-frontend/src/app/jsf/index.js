/**
 * This is to add React components to existing jsf page besides sidebar/legacy.
 * Created by pahuang on 6/23/17.
 */
import * as React from 'react'
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
  toggleTMMergeModal
} from '../actions/version-actions'
import {
  showExportTMXModal
} from '../actions/tmx-actions'

const logger = createLogger({
  predicate: (getState, action) =>
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
export default function mountReactComponent () {
  // Attaching to window object so modal can be triggered from the JSF page
  window.toggleTMMergeModal = () => store.dispatch(toggleTMMergeModal())
  window.toggleTMXExportModal = (show) =>
    store.dispatch(showExportTMXModal(show))
  const mountPoint = document.getElementById('jsfReactRoot')

  render(<JsfRoot store={store} history={enhancedHistory} />, mountPoint)
}
