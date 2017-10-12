import { ROUTING_PARAMS_CHANGED } from '../actions/action-types'
import { DEFAULT_LOCALE } from './ui-reducer'

const defaultState = {
  sourceLocale: DEFAULT_LOCALE
}

const routingParamsReducer = (state = defaultState, action) => {
  switch (action.type) {
    case ROUTING_PARAMS_CHANGED:
      return {...state, ...action.payload}
    default:
      return state
  }
}

export default routingParamsReducer
