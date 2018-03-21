import { createStore, applyMiddleware } from 'redux'
import { apiMiddleware } from 'redux-api-middleware'
import enhancedCallApi from './call-api'
import newContextFetchMiddleware from './new-context-fetch'
import getStateInActions from './getstate-in-actions'
import titleUpdateMiddleware from './title-update'
import thunk from 'redux-thunk'
import createLogger from 'redux-logger'

const DEV = process.env && process.env.NODE_ENV === 'development'
const logger = createLogger({
  // options
  // @ts-ignore
  actionTransformer: (action) => {
    return {
      ...action,
      // allow symbol action type to be printed properly in logs
      // TODO remove when types are migrated to stop using symbol
      type: String(action.type)
    }
  }
})

const middleware = [
  // TODO check if react helmet works here instead
  titleUpdateMiddleware,
  newContextFetchMiddleware,
  // reduxRouterMiddleware,
  DEV && require('redux-immutable-state-invariant').default(),
  thunk,
  enhancedCallApi,
  apiMiddleware,
  // must run after thunk because it fails with thunks
  getStateInActions,
  DEV && logger // must be last to avoid logging thunk/promise
].filter(Boolean)

const createStoreWithMiddleware = applyMiddleware(...middleware)(createStore)

export default createStoreWithMiddleware
