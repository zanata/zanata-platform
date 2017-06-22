import { CALL_API, getJSON } from 'redux-api-middleware'
import { createAction } from 'redux-actions'
import {
  getJsonHeaders,
  buildAPIRequest
} from './common-actions'
import { apiUrl } from '../config'

export const TOGGLE_TM_MERGE_MODAL = 'TOGGLE_TM_MERGE_MODAL'

export const VERSION_LOCALES_REQUEST = 'VERSION_LOCALES_REQUEST'
export const VERSION_LOCALES_SUCCESS = 'VERSION_LOCALES_SUCCESS'
export const VERSION_LOCALES_FAILURE = 'VERSION_LOCALES_FAILURE'

export const PROJECT_PAGE_REQUEST = 'PROJECT_PAGE_REQUEST'
export const PROJECT_PAGE_SUCCESS = 'PROJECT_PAGE_SUCCESS'
export const PROJECT_PAGE_FAILURE = 'PROJECT_PAGE_FAILURE'

export const toggleTMMergeModal =
    createAction(TOGGLE_TM_MERGE_MODAL)

const getProjectVersionLocales = (dispatch, project, version) => {
  const endpoint = `${apiUrl}/project/${project}/version/${version}/locales`
  const apiTypes = [
    VERSION_LOCALES_REQUEST,
    VERSION_LOCALES_SUCCESS,
    VERSION_LOCALES_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export const loadVersionLocales = (projectSlug, versionSlug) => {
  return (dispatch) => {
    dispatch(getProjectVersionLocales(dispatch, projectSlug, versionSlug))
  }
}

const getProjectPage = (dispatch, projectSearchTerm) => {
  const endpoint =
      `${apiUrl}/search/projects?q=${projectSearchTerm}&includeVersion=true`
  const apiTypes = [
    PROJECT_PAGE_REQUEST,
    {
      type: PROJECT_PAGE_SUCCESS,
      payload: (action, state, res) => {
        return getJSON(res).then((json) => json.results)
      }
    },
    PROJECT_PAGE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export const loadProjectPage = (projectSearchTerm) => {
  return (dispatch) => {
    dispatch(getProjectPage(dispatch, projectSearchTerm))
  }
}
