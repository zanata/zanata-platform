import { createStore, applyMiddleware } from 'redux'
import { apiMiddleware } from 'redux-api-middleware'
import newContextFetchMiddleware from './middlewares/new-context-fetch'
import searchSelectedPhraseMiddleware
  from './middlewares/selected-phrase-searches'
import getStateInActions from './middlewares/getstate-in-actions'
import titleUpdateMiddleware from './middlewares/title-update'
import thunk from 'redux-thunk'
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

export default createStoreWithMiddleware
