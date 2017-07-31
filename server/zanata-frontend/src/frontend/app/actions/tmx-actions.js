import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import { isUndefined, includes } from 'lodash'
import {
  getJsonHeaders,
  getHeaders,
  buildAPIRequest
} from './common-actions'
import { apiUrl, isLoggedIn } from '../config'

export const TMX_TYPE = ['all', 'project', 'version']

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

const fetchSourceLanguages = (endpoint) => {
  const apiTypes = [
    GET_LOCALE_REQUEST,
    {
      type: GET_LOCALE_SUCCESS,
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
    GET_LOCALE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const getTMX = (srcLocaleId, project, version, endpoint) => {
  const apiTypes = [
    {
      type: GET_TMX_REQUEST,
      payload: (action, state) => ({srcLocaleId: srcLocaleId})
    },
    {
      type: GET_TMX_SUCCESS,
      payload: (action, state, res) => {
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
      payload: (action, state) => ({srcLocaleId: srcLocaleId})
    }
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getHeaders(), apiTypes)
  }
}

export const tmxInitialLoad = (project, version) => {
  return (dispatch, getState) => {
    if (isLoggedIn) {
      let type
      if ((project || !isUndefined(project)) &&
          (!version || isUndefined(version))) {
        type = TMX_TYPE[1] // project type
      } else if ((project || !isUndefined(project)) &&
          (version || !isUndefined(version))) {
        type = TMX_TYPE[2] // project version type
      } else {
        type = TMX_TYPE[0] // all type
      }
      dispatch(setInitialState(type))
      let endpoint
      switch (type) {
        case TMX_TYPE[0]:
          // get all source language in all active documents
          endpoint = apiUrl + '/locales/source'
          dispatch(fetchSourceLanguages(endpoint))
          break
        case TMX_TYPE[1]:
          // get all source language in all active documents in project
          endpoint = apiUrl + '/projects/p/' + project + '/locales/source'
          dispatch(fetchSourceLanguages(endpoint))
          break
        case TMX_TYPE[2]:
          // get all source language in all active documents in project version
          endpoint = apiUrl + '/projects/p/' + project +
              '/iterations/i/' + version + '/locales/source'
          dispatch(fetchSourceLanguages(endpoint))
          break
      }
    }
  }
}

export const exportTMX = (localeId, project, version) => {
  return (dispatch, getState) => {
    const type = getState().tmx.tmxExport.type
    let endpoint
    switch (type) {
      case TMX_TYPE[0]:
        endpoint = apiUrl + '/tm/all' +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
      case TMX_TYPE[1]:
        endpoint = apiUrl + '/tm/projects/' + project +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
      case TMX_TYPE[2]:
        endpoint = apiUrl + '/tm/projects/' + project + '/iterations/' +
          version +
          (isUndefined(localeId) ? '' : ('?srcLocale=' + localeId))
        break
    }
    dispatch(getTMX(localeId, project, version, endpoint))
  }
}
