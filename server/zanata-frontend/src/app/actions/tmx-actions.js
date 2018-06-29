import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import { isUndefined, includes } from 'lodash'
import {
  getJsonHeaders,
  getHeaders,
  buildAPIRequest,
  // eslint-disable-next-line
  APITypes
} from './common-actions'
import { apiUrl, isLoggedIn } from '../config'

export const TMX_ALL = 'all'
export const TMX_PROJECT = 'project'
export const TMX_VERSION = 'version'

export const SHOW_EXPORT_TMX_MODAL = 'SHOW_EXPORT_TMX_MODAL'
export const SET_INITIAL_STATE = 'SET_INITIAL_STATE'

export const GET_LOCALE_REQUEST = 'GET_LOCALE_REQUEST'
export const GET_LOCALE_SUCCESS = 'GET_LOCALE_SUCCESS'
export const GET_LOCALE_FAILURE = 'GET_LOCALE_FAILURE'

export const GET_TMX_REQUEST = 'GET_TMX_REQUEST'
export const GET_TMX_SUCCESS = 'GET_TMX_SUCCESS'
export const GET_TMX_FAILURE = 'GET_TMX_FAILURE'

export const showExportTMXModal =
    createAction(SHOW_EXPORT_TMX_MODAL)
export const setInitialState = createAction(SET_INITIAL_STATE)

// @ts-ignore any
const fetchSourceLanguages = (endpoint) => {
  /** @type {APITypes} */
  const apiTypes = [
    GET_LOCALE_REQUEST,
    {
      type: GET_LOCALE_SUCCESS,
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
    GET_LOCALE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getTMX = (srcLocaleId, project, version, endpoint) => {
  /** @type {APITypes} */
  const apiTypes = [
    {
      type: GET_TMX_REQUEST,
      // @ts-ignore any
      payload: (_action, _state) => ({srcLocaleId: srcLocaleId})
    },
    {
      type: GET_TMX_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        // @ts-ignore any
        return res.blob().then((blob) => {
          return {blob, srcLocaleId, project, version}
        })
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    {
      type: GET_TMX_FAILURE,
      // @ts-ignore any
      payload: (_action, _state) => ({srcLocaleId: srcLocaleId})
    }
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getHeaders(), apiTypes)
  }
}

// @ts-ignore any
export const tmxInitialLoad = (project, version) => {
  // @ts-ignore any
  return (dispatch, _getState) => {
    if (isLoggedIn) {
      let type
      let endpoint
      if ((project || !isUndefined(project)) &&
          (!version || isUndefined(version))) {
        type = TMX_PROJECT
        endpoint = apiUrl + '/projects/p/' + project + '/locales/source'
      } else if ((project || !isUndefined(project)) &&
          (version || !isUndefined(version))) {
        type = TMX_VERSION
        endpoint = apiUrl + '/projects/p/' + project +
            '/iterations/i/' + version + '/locales/source'
      } else {
        type = TMX_ALL
        endpoint = apiUrl + '/locales/source'
      }
      // @ts-ignore
      dispatch(setInitialState(type))
      dispatch(fetchSourceLanguages(endpoint))
    }
  }
}

// @ts-ignore any
export const exportTMX = (localeId, project, version) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    const type = getState().tmx.tmxExport.type
    let endpoint
    switch (type) {
      case TMX_ALL:
        endpoint = apiUrl + '/tm/all' +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
      case TMX_PROJECT:
        endpoint = apiUrl + '/tm/projects/' + project +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
      case TMX_VERSION:
        endpoint = apiUrl + '/tm/projects/' + project + '/iterations/' +
          version +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
    }
    dispatch(getTMX(localeId, project, version, endpoint))
  }
}
