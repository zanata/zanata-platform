import { CALL_API } from 'redux-api-middleware'
import { createAction } from 'redux-actions'
import {
  getJsonHeaders,
  buildAPIRequest
} from './common-actions'
import { apiUrl } from '../config'

export const TOGGLE_TM_MERGE_MODAL = 'TOGGLE_TM_MERGE_MODAL'
export const toggleTMMergeModal =
  createAction(TOGGLE_TM_MERGE_MODAL)

export const VERSION_LOCALES_REQUEST = 'VERSION_LOCALES_REQUEST'
export const VERSION_LOCALES_SUCCESS = 'VERSION_LOCALES_SUCCESS'
export const VERSION_LOCALES_FAILURE = 'VERSION_LOCALES_FAILURE'

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
