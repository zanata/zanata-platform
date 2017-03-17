import { ROUTING_PARAMS_CHANGED } from '../actions'
import { DEFAULT_LOCALE } from './ui'

const defaultState = {
  sourceLocale: DEFAULT_LOCALE
}

const routingParamsReducer = (state = defaultState, action) => {
  switch (action.type) {
    case ROUTING_PARAMS_CHANGED:
      return {...state, ...action.params}
    default:
      return state
  }
}

export default routingParamsReducer
