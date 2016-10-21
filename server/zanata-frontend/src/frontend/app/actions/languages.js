import { CALL_API } from 'redux-api-middleware'
import { includes, isEmpty } from 'lodash'
import { replaceRouteQuery } from '../utils/RoutingHelpers'
import {
  getJsonHeaders,
  buildAPIRequest,
  LOAD_USER_REQUEST,
  LOAD_USER_SUCCESS,
  LOAD_USER_FAILURE
} from './common'

export const LOAD_LANGUAGES_REQUEST = 'LOAD_LANGUAGES_REQUEST'
export const LOAD_LANGUAGES_SUCCESS = 'LOAD_LANGUAGES_SUCCESS'
export const LOAD_LANGUAGES_FAILURE = 'LOAD_LANGUAGES_FAILURE'

export const LANGUAGE_PERMISSION_REQUEST = 'LANGUAGE_PERMISSION_REQUEST'
export const LANGUAGE_PERMISSION_SUCCESS = 'LANGUAGE_PERMISSION_SUCCESS'
export const LANGUAGE_PERMISSION_FAILURE = 'LANGUAGE_PERMISSION_FAILURE'

export const LANGUAGE_DELETE_REQUEST = 'LANGUAGE_DELETE_REQUEST'
export const LANGUAGE_DELETE_SUCCESS = 'LANGUAGE_DELETE_SUCCESS'
export const LANGUAGE_DELETE_FAILURE = 'LANGUAGE_DELETE_FAILURE'

export const pageSizeOption = [10, 20, 50, 100]
// export const sortOption = ['Locale', 'Members']
export const sortOption = [
  {display: 'Locale', value: 'localeId'},
  {display: 'Members', value: 'member'}
]

const getLocalesList = (state) => {
  const query = state.routing.location.query
  let queries = []
  if (query.search) {
    queries.push('filter=' + query.search)
  }
  if (query.page) {
    queries.push('page=' + query.page)
  }
  if (query.size) {
    queries.push('sizePerPage=' + query.size)
  }
  if (query.sort) {
    queries.push('sort=' + query.sort.toLowerCase())
  }
  const endpoint = window.config.baseUrl + window.config.apiRoot + '/locales' +
    (!isEmpty(queries) ? '?' + queries.join('&') : '')

  const apiTypes = [
    LOAD_LANGUAGES_REQUEST,
    {
      type: LOAD_LANGUAGES_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
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

const getLocalesPermission = (dispatch) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/user/permission/locales'

  const apiTypes = [
    LANGUAGE_PERMISSION_REQUEST,
    {
      type: LANGUAGE_PERMISSION_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
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

const getUserInfo = (dispatch) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot + '/user'

  const apiTypes = [
    LOAD_USER_REQUEST,
    {
      type: LOAD_USER_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
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

const deleteLanguage = (dispatch, localeId) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/locales/locale/' + localeId

  const apiTypes = [
    LANGUAGE_DELETE_REQUEST,
    {
      type: LANGUAGE_DELETE_SUCCESS,
      payload: (action, state, res) => {
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

export const initialLoad = () => {
  return (dispatch, getState) => {
    if (!window.config.permission.isLoggedIn) {
      dispatch(getLocalesList(getState()))
    } else {
      dispatch(getUserInfo(dispatch))
    }
  }
}

export const handleUpdatePageSize = (pageSize) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {
      size: pageSize
    })
    dispatch(getLocalesList(getState()))
  }
}

export const handleUpdateSort = (sort) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {
      sort: sort
    })
    dispatch(getLocalesList(getState()))
  }
}

export const handleUpdateSearch = (search) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {
      search: search
    })
    dispatch(getLocalesList(getState()))
  }
}

export const handleDelete = (localeId) => {
  return (dispatch, getState) => {
    dispatch(deleteLanguage(dispatch, localeId))
  }
}

export const handlePageUpdate = (page) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {
      page: page
    })
    dispatch(getLocalesList(getState()))
  }
}
