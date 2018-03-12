import { CALL_API, getJSON } from 'redux-api-middleware'
import { createAction } from 'redux-actions'
import {
  getJsonHeaders,
  buildAPIRequest,
  // eslint-disable-next-line
  APITypes
} from './common-actions'
import { apiUrl } from '../config'
import {replace} from 'lodash'
import {toInternalTMSource} from '../utils/EnumValueUtils'

import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_REQUEST,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_REQUEST,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE,
  VERSION_TM_MERGE_REQUEST,
  VERSION_TM_MERGE_SUCCESS,
  VERSION_TM_MERGE_FAILURE,
  QUERY_TM_MERGE_PROGRESS_REQUEST,
  QUERY_TM_MERGE_PROGRESS_SUCCESS,
  QUERY_TM_MERGE_PROGRESS_FAILURE,
  TM_MERGE_CANCEL_REQUEST,
  TM_MERGE_CANCEL_SUCCESS,
  TM_MERGE_CANCEL_FAILURE,
  TM_MERGE_PROCESS_FINISHED
} from './version-action-types'

/** Open or close the TM Merge modal  */
export const toggleTMMergeModal =
    createAction(TOGGLE_TM_MERGE_MODAL)

/**
 * Fetch project version specific locales from database
 *
 * @param project projectSlug
 * @param version versionSlug
 * */
export const fetchVersionLocales = (project, version) => {
  const endpoint = `${apiUrl}/project/${project}/version/${version}/locales`
  /** @type {APITypes} */
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
  // used for success/failure to ensure the most recent results are used
  const timestamp = Date.now()
  // empty search term should return nothing
  if (!projectSearchTerm) {
    return {
      type: PROJECT_PAGE_SUCCESS,
      meta: {timestamp},
      payload: []
    }
  }
  // making the call to server
  const endpoint =
      `${apiUrl}/search/projects?q=${projectSearchTerm}&includeVersion=true`
  /** @type {APITypes} */
  const apiTypes = [
    PROJECT_PAGE_REQUEST,
    {
      type: PROJECT_PAGE_SUCCESS,
      meta: {timestamp},
      payload: (_action, _state, res) => {
        return getJSON(res).then((json) => json.results)
      }
    },
    {
      type: PROJECT_PAGE_FAILURE,
      meta: {timestamp}
    }
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// convert project version to string representation
const toProjectVersionString = (projectVersion) => {
  return `${projectVersion.projectSlug}/${projectVersion.version.id}`
}

/* eslint-disable max-len */
/**
 * @param {string} projectSlug target project slug
 * @param {string} versionSlug target version slug
 * @param {{
     matchPercentage: number,
     differentProject: boolean,
     differentDocId: boolean,
     differentContext: boolean,
     fromImportedTM: boolean,
     fromAllProjects: boolean,
     selectedLanguage: {localeId: string, displayName: string},
     selectedVersions: Array.<{projectSlug: string, version: {id: string}}>
   }} mergeOptions
 * @returns redux api action object
 */
/* eslint-enable max-len */
export function mergeVersionFromTM (projectSlug, versionSlug, mergeOptions) {
  const endpoint =
    `${apiUrl}/project/${projectSlug}/version/${versionSlug}/tm-merge`
  /** @type {APITypes} */
  const types = [VERSION_TM_MERGE_REQUEST,
    {
      type: VERSION_TM_MERGE_SUCCESS,
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && ~contentType.indexOf('json')) {
          // Just making sure res.json() does not raise an error
          return res.json().then((json) => {
            const cancelUrl = replace(json.url,
                '/rest/process/', '/rest/process/cancel/')
            return {...json, cancelUrl}
          })
        }
      }
    }, VERSION_TM_MERGE_FAILURE]
  const {
    selectedLanguage: {localeId},
    matchPercentage,
    differentProject,
    differentDocId,
    differentContext,
    fromImportedTM,
    fromAllProjects,
    selectedVersions
  } = mergeOptions

  const internalTMSource = toInternalTMSource(
    fromAllProjects, selectedVersions.map(toProjectVersionString))
  const body = {
    localeId: localeId,
    thresholdPercent: matchPercentage,
    differentProjectRule: differentProject,
    differentDocumentRule: differentDocId,
    differentContextRule: differentContext,
    importedMatchRule: fromImportedTM,
    internalTMSource: internalTMSource
  }
  const apiRequest = buildAPIRequest(
    endpoint, 'POST', getJsonHeaders(), types, JSON.stringify(body)
  )
  return {
    [CALL_API]: apiRequest
  }
}

export function queryTMMergeProgress (url) {
  /** @type {APITypes} */
  const types = [QUERY_TM_MERGE_PROGRESS_REQUEST,
    QUERY_TM_MERGE_PROGRESS_SUCCESS,
    QUERY_TM_MERGE_PROGRESS_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(url, 'GET', getJsonHeaders(), types)
  }
}

export function cancelTMMergeRequest (url) {
  /** @type {APITypes} */
  const types = [
    TM_MERGE_CANCEL_REQUEST,
    TM_MERGE_CANCEL_SUCCESS,
    TM_MERGE_CANCEL_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(url, 'POST', getJsonHeaders(), types)
  }
}

export const currentTMMergeProcessFinished =
  createAction(TM_MERGE_PROCESS_FINISHED)
