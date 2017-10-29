import { handleActions } from 'redux-actions'
import {
  CLEAR_MESSAGE,
  DEFAULT_LOCALE
} from '../actions/common-actions'

export default handleActions({
  [CLEAR_MESSAGE]: (state, action) => {
    return {
      ...state,
      notification: null
    }
  }
},
// default state
  {
    locales: [],
    loading: false,
    selectedLocale: DEFAULT_LOCALE.localeId
  })
