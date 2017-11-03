import { handleAction } from 'redux-actions'
import { ROUTING_PARAMS_CHANGED } from '../actions/action-types'
import { DEFAULT_LOCALE } from './ui-reducer'

const defaultState = {
  sourceLocale: DEFAULT_LOCALE
}

const contextReducer = handleAction(
  ROUTING_PARAMS_CHANGED,
  (state, { payload }) => ({...state, ...payload}),
  defaultState
)

export default contextReducer
