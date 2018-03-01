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
  LOCALE_MESSAGES_REQUEST,
  LOCALE_MESSAGES_SUCCESS,
  LOCALE_MESSAGES_FAILURE
} from '../actions/header-action-types'
import {
  TRANS_HISTORY_REQUEST,
  TRANS_HISTORY_SUCCESS,
  TRANS_HISTORY_FAILURE
} from '../actions/activity-action-types'
import { buildAPIRequest, getJsonHeaders } from '../../actions/common-actions'
import { CALL_API } from 'redux-api-middleware'
import { includes } from 'lodash'
import { apiUrl, serverUrl, appUrl } from '../../config'
import stableStringify from 'faster-stable-stringify'

export const dashboardUrl = serverUrl + '/dashboard'

export function projectPageUrl (projectSlug, versionSlug) {
  return `${serverUrl}/iteration/view/${projectSlug}/${versionSlug}`
}

export function profileUrl (username) {
  return `${serverUrl}/profile/view/${username}`
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

export function fetchI18nLocale (locale) {
  const endpoint = `${appUrl}/messages/${locale}.json`
  const apiTypes = [
    LOCALE_MESSAGES_REQUEST,
    {
      type: LOCALE_MESSAGES_SUCCESS,
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return json
          })
        }
      }
    },
    LOCALE_MESSAGES_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export function fetchTransUnitHistory (
  localeId, transUnitId, projectSlug, versionSlug) {
  // eslint-disable-next-line max-len
  const endpoint = `${apiUrl}/transhist/${localeId}/${transUnitId}/${projectSlug}?versionSlug=${versionSlug}`
  const apiTypes = [
    TRANS_HISTORY_REQUEST,
    {
      type: TRANS_HISTORY_SUCCESS,
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return json
          })
        }
      }
    },
    TRANS_HISTORY_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
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
