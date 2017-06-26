import { CALL_API, getJSON } from 'redux-api-middleware'
import { createAction } from 'redux-actions'
import {
  getJsonHeaders,
  buildAPIRequest
} from './common-actions'
import { apiUrl } from '../config'

import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_REQUEST,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_REQUEST,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE
} from './version-action-types'

/** Toggle application state boolean determining whether modal is displayed */
export const toggleTMMergeModal =
    createAction(TOGGLE_TM_MERGE_MODAL)

/**
 * Fetch project version specific locales from database
 *
 * @param projectSlug project ID
 * @param versionSlug version ID
 * */
export const fetchVersionLocales = (projectSlug, versionSlug) => {
  return (dispatch) => {
    dispatch(fetchProjectVersionLocales(dispatch, projectSlug, versionSlug))
  }
}

function fetchProjectVersionLocales (dispatch, project, version) {
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

/**
 * Fetch projects to merge from database
 *
 * @param projectSearchTerm to filter results
 * */
export const fetchProjectPage = (projectSearchTerm) => {
  return (dispatch) => {
    dispatch(fetchSearchProjectPage(dispatch, projectSearchTerm))
  }
}

function fetchSearchProjectPage (dispatch, projectSearchTerm) {
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
