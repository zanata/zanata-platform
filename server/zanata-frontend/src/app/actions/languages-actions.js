import { CALL_API } from 'redux-api-middleware'
import { createAction } from 'redux-actions'
import { includes, isEmpty } from 'lodash'
import { replaceRouteQuery } from '../utils/RoutingHelpers'
import {
  getJsonHeaders,
  buildAPIRequest,
  LOAD_USER_REQUEST,
  LOAD_USER_SUCCESS,
  LOAD_USER_FAILURE,
  // eslint-disable-next-line
  APITypes
} from './common-actions'
import { apiUrl, isLoggedIn } from '../config'

export const LOAD_LANGUAGES_REQUEST = 'LOAD_LANGUAGES_REQUEST'
export const LOAD_LANGUAGES_SUCCESS = 'LOAD_LANGUAGES_SUCCESS'
export const LOAD_LANGUAGES_FAILURE = 'LOAD_LANGUAGES_FAILURE'

export const LOAD_LANGUAGES_SUGGESTION_REQUEST =
  'LOAD_LANGUAGES_SUGGESTION_REQUEST'
export const LOAD_LANGUAGES_SUGGESTION_SUCCESS =
  'LOAD_LANGUAGES_SUGGESTION_SUCCESS'
export const LOAD_LANGUAGES_SUGGESTION_FAILURE =
  'LOAD_LANGUAGES_SUGGESTION_FAILURE'

export const LANGUAGE_PERMISSION_REQUEST = 'LANGUAGE_PERMISSION_REQUEST'
export const LANGUAGE_PERMISSION_SUCCESS = 'LANGUAGE_PERMISSION_SUCCESS'
export const LANGUAGE_PERMISSION_FAILURE = 'LANGUAGE_PERMISSION_FAILURE'

export const CREATE_LANGUAGE_REQUEST = 'CREATE_LANGUAGE_REQUEST'
export const CREATE_LANGUAGE_SUCCESS = 'CREATE_LANGUAGE_SUCCESS'
export const CREATE_LANGUAGE_FAILURE = 'CREATE_LANGUAGE_FAILURE'

export const LANGUAGE_DELETE_REQUEST = 'LANGUAGE_DELETE_REQUEST'
export const LANGUAGE_DELETE_SUCCESS = 'LANGUAGE_DELETE_SUCCESS'
export const LANGUAGE_DELETE_FAILURE = 'LANGUAGE_DELETE_FAILURE'

export const pageSizeOption = [10, 20, 50, 100]
export const sortOption = [
  {display: 'Locale (a-z)', value: 'localeId'},
  {display: 'Locale (z-a)', value: '-localeId'},
  {display: 'Members (low-high)', value: 'member'},
  {display: 'Members (high-low)', value: '-member'}
]

// @ts-ignore any
const getLocalesList = (state) => {
  const query = state.routing.locationBeforeTransitions.query
  let queries = []
  if (query.search) {
    queries.push('filter=' + encodeURIComponent(query.search))
  }
  if (query.page) {
    queries.push('page=' + query.page)
  }
  if (query.size) {
    queries.push('sizePerPage=' + query.size)
  }
  if (query.sort) {
    queries.push('sort=' + query.sort)
  }
  const endpoint = apiUrl + '/locales' +
    (!isEmpty(queries) ? '?' + queries.join('&') : '')

  /** @type {APITypes} */
  const apiTypes = [
    LOAD_LANGUAGES_REQUEST,
    {
      type: LOAD_LANGUAGES_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LOAD_LANGUAGES_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const searchLocales = (query) => {
  const endpoint = apiUrl + '/locales/new?filter=' + encodeURIComponent(query)

  /** @type {APITypes} */
  const apiTypes = [
    LOAD_LANGUAGES_SUGGESTION_REQUEST,
    {
      type: LOAD_LANGUAGES_SUGGESTION_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LOAD_LANGUAGES_SUGGESTION_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getLocalesPermission = (dispatch) => {
  const endpoint = apiUrl + '/user/permission/locales'

  /** @type {APITypes} */
  const apiTypes = [
    LANGUAGE_PERMISSION_REQUEST,
    {
      type: LANGUAGE_PERMISSION_SUCCESS,
      // @ts-ignore any
      payload: (_action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            dispatch(getLocalesList(state))
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LANGUAGE_PERMISSION_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getUserInfo = (dispatch) => {
  const endpoint = apiUrl + '/user'

  /** @type {APITypes} */
  const apiTypes = [
    LOAD_USER_REQUEST,
    {
      type: LOAD_USER_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            dispatch(getLocalesPermission(dispatch))
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LOAD_USER_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const deleteLanguage = (dispatch, localeId) => {
  const endpoint = apiUrl + '/locales/locale/' + localeId
  /** @type {APITypes} */
  const apiTypes = [
    LANGUAGE_DELETE_REQUEST,
    {
      type: LANGUAGE_DELETE_SUCCESS,
      // @ts-ignore any
      payload: (_action, state, res) => {
        dispatch(getLocalesList(state))
        return res
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LANGUAGE_DELETE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'DELETE', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const createNewLanguage = (details, dispatch) => {
  const endpoint = apiUrl + '/locales/locale'
  let headers = getJsonHeaders()
  headers['Content-Type'] = 'application/json'

  /** @type {APITypes} */
  const apiTypes = [
    CREATE_LANGUAGE_REQUEST,
    {
      type: CREATE_LANGUAGE_SUCCESS,
      // @ts-ignore any
      payload: (_action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            dispatch(getLocalesList(state))
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    CREATE_LANGUAGE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', headers, apiTypes,
      JSON.stringify(details))
  }
}

export const initialLoad = () => {
  // @ts-ignore any
  return (dispatch, getState) => {
    // validate page number from query
    const page = parseInt(
      getState().routing.locationBeforeTransitions.query.page)
    if (page && page <= 1) {
      replaceRouteQuery(getState().routing.locationBeforeTransitions, {
        page: 1
      })
    }

    // validate page size from query
    const pageSize = parseInt(
      getState().routing.locationBeforeTransitions.query.size)
    if (pageSize && !includes(pageSizeOption, pageSize)) {
      replaceRouteQuery(getState().routing.locationBeforeTransitions, {
        size: pageSizeOption[0]
      })
    }
    if (!isLoggedIn) {
      dispatch(getLocalesList(getState()))
    } else {
      dispatch(getUserInfo(dispatch))
    }
  }
}

// @ts-ignore any
export const handleUpdatePageSize = (pageSize) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.locationBeforeTransitions, {
      size: pageSize
    })
    dispatch(getLocalesList(getState()))
  }
}

// @ts-ignore any
export const handleUpdateSort = (sort) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.locationBeforeTransitions, {
      sort: sort
    })
    dispatch(getLocalesList(getState()))
  }
}

// @ts-ignore any
export const handleUpdateSearch = (search) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.locationBeforeTransitions, {
      search: search,
      page: 1
    })
    dispatch(getLocalesList(getState()))
  }
}

// @ts-ignore any
export const handleDelete = (localeId) => {
  // @ts-ignore any
  return (dispatch, _getState) => {
    dispatch(deleteLanguage(dispatch, localeId))
  }
}

// @ts-ignore any
export const handlePageUpdate = (page) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.locationBeforeTransitions, {
      page: page
    })
    dispatch(getLocalesList(getState()))
  }
}

export const TOGGLE_NEW_LANGUAGE_DISPLAY = 'TOGGLE_NEW_LANGUAGE_DISPLAY'
export const handleNewLanguageDisplay =
  createAction(TOGGLE_NEW_LANGUAGE_DISPLAY)

// @ts-ignore any
export const handleLoadSuggestion = (query) => {
  // @ts-ignore any
  return (dispatch, _getState) => {
    if (!isEmpty(query)) {
      dispatch(searchLocales(query))
    }
  }
}

// @ts-ignore any
export const handleSaveNewLanguage = (details) => {
  // @ts-ignore any
  return (dispatch, _getState) => {
    dispatch(createNewLanguage(details, dispatch))
  }
}
