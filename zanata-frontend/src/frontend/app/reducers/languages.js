import { handleActions } from 'redux-actions'
import { keyBy } from 'lodash'
import {
  CLEAR_MESSAGE,
  SEVERITY,
  LOAD_USER_REQUEST,
  LOAD_USER_SUCCESS,
  LOAD_USER_FAILURE
} from '../actions/common'
import {
  LOAD_LANGUAGES_REQUEST,
  LOAD_LANGUAGES_SUCCESS,
  LOAD_LANGUAGES_FAILURE,
  LANGUAGE_PERMISSION_REQUEST,
  LANGUAGE_PERMISSION_SUCCESS,
  LANGUAGE_PERMISSION_FAILURE,
  LANGUAGE_DELETE_REQUEST,
  LANGUAGE_DELETE_SUCCESS,
  LANGUAGE_DELETE_FAILURE,
  TOGGLE_NEW_LANGUAGE_DISPLAY,
  LOAD_LANGUAGES_SUGGESTION_REQUEST,
  LOAD_LANGUAGES_SUGGESTION_SUCCESS,
  LOAD_LANGUAGES_SUGGESTION_FAILURE
} from '../actions/languages'

const ERROR_MSG = 'We were unable load languages from server. ' +
  'Please refresh this page and try again.'
export default handleActions({
  [TOGGLE_NEW_LANGUAGE_DISPLAY]: (state, action) => {
    return {
      ...state,
      newLanguage: {
        ...state.newLanguage,
        show: action.payload
      }
    }
  },
  [CLEAR_MESSAGE]: (state, action) => {
    return {
      ...state,
      notification: null
    }
  },
  [LOAD_USER_REQUEST]: (state, action) => {
    return {
      ...state,
      user: {
        ...state.user,
        loading: true
      }
    }
  },
  [LOAD_USER_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        user: {
          ...state.user,
          loading: true
        },
        loading: false,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      const languageTeams =
        keyBy(action.payload.languageTeams, function (localeId) {
          return localeId
        })
      return {
        ...state,
        user: {
          ...action.payload,
          languageTeams: languageTeams
        }
      }
    }
  },
  [LOAD_USER_FAILURE]: (state, action) => {
    return {
      ...state,
      user: {
        ...state.user,
        loading: false
      },
      loading: false,
      notification: {
        severity: SEVERITY.ERROR,
        message: ERROR_MSG
      }
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
  },
  [LANGUAGE_DELETE_REQUEST]: (state, action) => {
    return {
      ...state,
      deleting: true
    }
  },
  [LANGUAGE_DELETE_SUCCESS]: (state, action) => {
    return {
      ...state,
      deleting: false
    }
  },
  [LANGUAGE_DELETE_FAILURE]: (state, action) => {
    return {
      ...state,
      deleting: false,
      notification: {
        severity: SEVERITY.ERROR,
        message: 'We were unable delete this language. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [LOAD_LANGUAGES_SUGGESTION_REQUEST]: (state, action) => {
    return {
      ...state,
      searchResults: []
    }
  },
  [LOAD_LANGUAGES_SUGGESTION_SUCCESS]: (state, action) => {
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
        newLanguage: {
          ...state.newLanguage,
          searchResults: action.payload.results
        }
      }
    }
  },
  [LOAD_LANGUAGES_SUGGESTION_FAILURE]: (state, action) => {
    return {
      ...state,
      newLanguage: {
        ...state.newLanguage,
        searchResults: []
      },
      notification: {
        severity: SEVERITY.ERROR,
        message: ERROR_MSG
      }
    }
  }
},
  {
    user: {},
    loading: true,
    locales: {
      results: [],
      totalCount: 0
    },
    newLanguage: {
      saving: false,
      show: false,
      details: {},
      searchResults: []
    },
    permission: {
      canDeleteLocale: false,
      canAddLocale: false
    },
    deleting: false
  })
