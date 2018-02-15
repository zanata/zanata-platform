/**
 * Helper functions to make requests on the REST API to a Zanata server
 */

// The relevant docs for this fetch are at
// https://www.npmjs.com/package/whatwg-fetch
// (it is just a wrapper around whatwg-fetch)
import fetch from 'isomorphic-fetch'
import { encode } from '../utils/doc-id-util'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED,
  STATUS_APPROVED,
  STATUS_REJECTED
} from '../utils/status-util'
import {
  USER_PERMISSION_REQUEST,
  USER_PERMISSION_SUCCESS,
  USER_PERMISSION_FAILURE
} from '../actions/header-action-types'
import { buildAPIRequest, getJsonHeaders } from '../../actions/common-actions'
import { CALL_API } from 'redux-api-middleware'
import { includes } from 'lodash'
import { apiUrl, serverUrl } from '../../config'
import stableStringify from 'faster-stable-stringify'

export const dashboardUrl = serverUrl + '/dashboard'

export function projectPageUrl (projectSlug, versionSlug) {
  return `${serverUrl}/iteration/view/${projectSlug}/${versionSlug}`
}

export function profileUrl (username) {
  return `${serverUrl}/profile/view/${username}`
}

export function getUserPermissions (localeId, projectSlug) {
  const endpoint =
  `${apiUrl}/user/permission/roles/project/${projectSlug}?localeId=${localeId}`
  const apiTypes = [
    USER_PERMISSION_REQUEST,
    {
      type: USER_PERMISSION_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return json
          })
        }
      }
    },
    USER_PERMISSION_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export function fetchStatistics (projectSlug, versionSlug, docId, localeId) {
  const statsUrl =
    `${apiUrl}/stats/project/${projectSlug}/version/${versionSlug}/doc/${encode(docId)}/locale/${localeId}` // eslint-disable-line max-len

  return fetch(statsUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'
  })
}

export function fetchLocales () {
  // TODO pahuang this was using $location to build up the ui locales
  const uiTranslationsURL = `${apiUrl}/locales/ui`

  return fetch(uiTranslationsURL, {
    credentials: 'include',
    method: 'GET'
  })
}

export function fetchMyInfo () {
  const userUrl = `${apiUrl}/user`
  return fetch(userUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'

  })
}

export function fetchProjectInfo (projectSlug) {
  const projectUrl = `${apiUrl}/project/${projectSlug}`
  return fetch(projectUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'

  })
}

export function fetchDocuments (projectSlug, versionSlug) {
  const docListUrl =
    `${apiUrl}/project/${projectSlug}/version/${versionSlug}/docs`
  return fetch(docListUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'
  })
}

export function fetchVersionLocales (projectSlug, versionSlug) {
  const localesUrl =
    `${apiUrl}/project/${projectSlug}/version/${versionSlug}/locales`
  return fetch(localesUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'
  })
}

export function savePhrase ({ id, revision, plural },
                            { localeId, status, translations }) {
  const translationUrl = `${apiUrl}/trans/${localeId}`
  return fetch(translationUrl, {
    credentials: 'include',
    method: 'PUT',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors',
    body: stableStringify({
      id,
      revision,
      plural,
      content: translations[0],
      contents: translations,
      status: phraseStatusToTransUnitStatus(status)
    })
  })
}

/**
 * Convert from lowercase phrase status used in redux app
 * to the caps-case strings used in the REST interface.
 */
function phraseStatusToTransUnitStatus (status) {
  switch (status) {
    case STATUS_UNTRANSLATED:
      return 'New'
    case STATUS_NEEDS_WORK:
      return 'NeedReview'
    case STATUS_TRANSLATED:
      return 'Translated'
    case STATUS_APPROVED:
      return 'Approved'
    case STATUS_REJECTED:
      return 'Rejected'
    default:
      console.error('Save attempt with invalid status', status)
  }
}
