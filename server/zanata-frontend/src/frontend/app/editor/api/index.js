/**
 * Helper functions to make requests on the REST API to a Zanata server
 */

// The relevant docs for this fetch are at
// https://www.npmjs.com/package/whatwg-fetch
// (it is just a wrapper around whatwg-fetch)
import fetch from 'isomorphic-fetch'
import { encode } from '../utils/doc-id-util'
import { chain, isEmpty } from 'lodash'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED,
  STATUS_APPROVED
} from '../utils/status-util'
import config, { baseUrl } from '../config'

/* The URL of this editor app. Used as a base for all URLs in the app. */
export const serviceUrl = getServiceUrl()

export const dashboardUrl = serviceUrl + '/dashboard'

/* The URL for the server where the REST API is deployed. Defaults to the
 * current server if there is nothing specified in the config. */
const apiOrigin = config.apiOrigin || serviceUrl

/* The URL where the REST API is deployed.
 * Used as a base for all REST URLs used by the API */
const apiRoot = config.apiRoot || ''
export const baseRestUrl = apiOrigin + apiRoot

/**
 * @returns Root Zanata url with context path.
 */
function getServiceUrl () {
  let serviceUrl = location.origin + baseUrl
  serviceUrl = serviceUrl.replace(/\/?$/, '') // remove trailing slash
  return serviceUrl
}

export function fetchPhraseList (project, version, localeId, docId, filter) {
  // FIXME damason check that arguments are all defined
  const encodedId = encode(docId)
  const queryString = filter ? filterQueryString(filter) : ''
  const statusListUrl =
    `${baseRestUrl}/project/${project}/version/${version}/doc/${encodedId}/status/${localeId}?${queryString}` // eslint-disable-line max-len

  return fetch(statusListUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'
  })
}

export function filterQueryString (advancedFilter) {
  return chain(advancedFilter)
    .toPairs()
    .filter(([key, value]) => !isEmpty(value))
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join('&')
    .value()
}

export function fetchPhraseDetail (localeId, phraseIds) {
  const phraseDetailUrl =
    `${baseRestUrl}/source+trans/${localeId}?ids=${phraseIds.join(',')}`

  return fetch(phraseDetailUrl, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors'
  })
}

export function fetchStatistics (_projectSlug, _versionSlug,
                                          _docId, _localeId) {
  const statsUrl =
    `${baseRestUrl}/stats/project/${_projectSlug}/version/${_versionSlug}/doc/${encode(_docId)}/locale/${_localeId}` // eslint-disable-line max-len

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
  const uiTranslationsURL = `${baseRestUrl}/locales/ui`

  return fetch(uiTranslationsURL, {
    credentials: 'include',
    method: 'GET'
  })
}

export function fetchMyInfo () {
  const userUrl = `${baseRestUrl}/user`
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
  const projectUrl = `${baseRestUrl}/project/${projectSlug}`
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
    `${baseRestUrl}/project/${projectSlug}/version/${versionSlug}/docs`
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
    `${baseRestUrl}/project/${projectSlug}/version/${versionSlug}/locales`
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
  const translationUrl = `${baseRestUrl}/trans/${localeId}`
  return fetch(translationUrl, {
    credentials: 'include',
    method: 'PUT',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    mode: 'cors',
    body: JSON.stringify({
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
    default:
      console.error('Save attempt with invalid status', status)
  }
}
