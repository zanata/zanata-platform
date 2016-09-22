import { handleActions } from 'redux-actions'
import {
  CLEAR_MESSAGE,
  SEVERITY
} from '../actions/common'
import {
  LOAD_LANGUAGES_REQUEST,
  LOAD_LANGUAGES_SUCCESS,
  LOAD_LANGUAGES_FAILURE,
  LANGUAGE_PERMISSION_REQUEST,
  LANGUAGE_PERMISSION_SUCCESS,
  LANGUAGE_PERMISSION_FAILURE
} from '../actions/languages'

const ERROR_MSG = 'We were unable load languages from server. ' +
  'Please refresh this page and try again.'
export default handleActions({
  [CLEAR_MESSAGE]: (state, action) => {
    return {
      ...state,
      notification: null
    }
  },
  [LANGUAGE_PERMISSION_REQUEST]: (state, action) => {
    return {
      ...state,
      loading: true
    }
  },
  [LANGUAGE_PERMISSION_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        loading: false,
        permission: {
          canDeleteLocale: false,
          canAddLocale: false
        },
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        permission: {
          canDeleteLocale: action.payload.canDeleteLocale,
          canAddLocale: action.payload.canAddLocale
        },
        loading: false
      }
    }
  },
  [LANGUAGE_PERMISSION_FAILURE]: (state, action) => {
    return {
      ...state,
      loading: false,
      permission: {
        canDeleteLocale: false,
        canAddLocale: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message: ERROR_MSG
      }
    }
  },
  [LOAD_LANGUAGES_REQUEST]: (state, action) => {
    return {
      ...state,
      loading: true
    }
  },
  [LOAD_LANGUAGES_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        loading: false,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        locales: action.payload,
        loading: false
      }
    }
  },
  [LOAD_LANGUAGES_FAILURE]: (state, action) => {
    return {
      ...state,
      loading: false,
      notification: {
        severity: SEVERITY.ERROR,
        message: ERROR_MSG
      }
    }
  }
},
  {
    loading: false,
    locales: [],
    permission: {
      canDeleteLocale: false,
      canAddLocale: false
    }
  })
