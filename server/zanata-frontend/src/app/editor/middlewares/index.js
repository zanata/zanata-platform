import { createStore, applyMiddleware } from 'redux'
import { apiMiddleware } from 'redux-api-middleware'
import enhancedCallApi from './call-api'
import newContextFetchMiddleware from './new-context-fetch'
import getStateInActions from './getstate-in-actions'
import titleUpdateMiddleware from './title-update'
const thunk /* TS: import thunk */ = require('redux-thunk')
import createLogger from 'redux-logger'

const loggerMiddleware = createLogger({
  predicate: (getState, action) =>
    process.env && (process.env.NODE_ENV === 'development'),
  actionTransformer: (action) => {
    return {
      ...action,
      // allow symbol action type to be printed properly in logs
      // TODO remove when types are migrated to stop using symbol
      type: String(action.type)
    }
  }
})

const createStoreWithMiddleware =
  applyMiddleware(
    // TODO check if react helmet works here instead
    titleUpdateMiddleware,
    newContextFetchMiddleware,
    // reduxRouterMiddleware,
    thunk,
    enhancedCallApi,
    apiMiddleware,
    // must run after thunk because it fails with thunks
    getStateInActions,
    loggerMiddleware
  )(createStore)

export default createStoreWithMiddleware
