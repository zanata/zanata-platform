import { handleActions } from 'redux-actions'
import {
  CLEAR_MESSAGE,
  DEFAULT_LOCALE
} from '../actions/common-actions'

export default handleActions({
  [CLEAR_MESSAGE]: (state, _action) => {
    return {
      ...state,
      notification: undefined
    }
  }
},
// default state
  {
    locales: [],
    loading: false,
    selectedLocale: DEFAULT_LOCALE.localeId
  })
