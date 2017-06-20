import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import { saveAs } from 'file-saver'
import { isUndefined, includes } from 'lodash'
import {
  getJsonHeaders,
  getHeaders,
  buildAPIRequest
} from './common-actions'
import { apiUrl } from '../config'

export const TMX_TYPE = ['all', 'project', 'version']

export const SHOW_EXPORT_TMX_MODAL = 'SHOW_EXPORT_TMX_MODAL'
export const TOGGLE_SHOW_SOURCE_LANGUAGES =
    'TOGGLE_SHOW_SOURCE_LANGUAGES'
export const SET_INITIAL_STATE = 'SET_INITIAL_STATE'

export const GET_LOCALE_REQUEST = 'GET_LOCALE_REQUEST'
export const GET_LOCALE_SUCCESS = 'GET_LOCALE_SUCCESS'
export const GET_LOCALE_FAILURE = 'GET_LOCALE_FAILURE'

export const GET_TMX_REQUEST = 'GET_TMX_REQUEST'
export const GET_TMX_SUCCESS = 'GET_TMX_SUCCESS'
export const GET_TMX_FAILURE = 'GET_TMX_FAILURE'

export const showExportTMXModal =
    createAction(SHOW_EXPORT_TMX_MODAL)
export const toggleShowSourceLanguages =
    createAction(TOGGLE_SHOW_SOURCE_LANGUAGES)

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

const buildTMXFileName = (project, version, locale) => {
  var p = !isUndefined(project) ? project : 'allProjects'
  var i = !isUndefined(version) ? version : 'allVersions'
  var l = !isUndefined(locale) ? locale.getId() : 'allLocales'
  return 'zanata-' + p + '-' + i + '-' + l + '.tmx'
}

const getTMX = (endpoint) => {
  const apiTypes = [
    GET_TMX_REQUEST,
    {
      type: GET_TMX_SUCCESS,
      payload: (action, state, res) => {
        const { project, version } = state.tmx
        return res.blob().then((blob) => {
          let filename = buildTMXFileName(project, version, undefined)
          saveAs(blob, filename)
        })
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    GET_TMX_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getHeaders(), apiTypes)
  }
}

// get all source language in all active documents
const fetchAllSourceLanguages = () => {
  const endpoint = apiUrl + '/locales/source'
  return fetchSourceLanguages(endpoint)
}

// get all source language in all active documents in project
const fetchProjectSourceLanguages = (project) => {
  const endpoint = apiUrl + '/projects/p/' + project + '/locales/source'
  return fetchSourceLanguages(endpoint)
}

// get all source language in all active documents in project version
const fetchVersionSourceLanguages = (project, version) => {
  const endpoint = apiUrl + '/projects/p/' + project +
      '/iterations/i/' + version + '/locales/source'
  return fetchSourceLanguages(endpoint)
}

export const tmxInitialLoad = (type, project, version) => {
  return (dispatch, getState) => {
    dispatch(setInitialState({type, project, version}))
    switch (type) {
      case TMX_TYPE[0]:
        dispatch(fetchAllSourceLanguages())
        break
      case TMX_TYPE[1]:
        dispatch(fetchProjectSourceLanguages(project))
        break
      case TMX_TYPE[2]:
        dispatch(fetchVersionSourceLanguages(project, version))
        break
    }
  }
}

export const exportTMX = () => {
  return (dispatch, getState) => {
    const state = getState().tmx
    const type = state.tmxExport.type
    let endpoint
    switch (type) {
      case TMX_TYPE[0]:
        endpoint = apiUrl + '/tm/all'
        break
      case TMX_TYPE[1]:
        endpoint = apiUrl + '/tm/projects/' + state.project
        break
      case TMX_TYPE[2]:
        endpoint = apiUrl + '/tm/projects/' + state.project +
                '/iterations/' + state.version
        break
    }
    dispatch(getTMX(endpoint))
  }
}
