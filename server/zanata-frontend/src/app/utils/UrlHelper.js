// @ts-nocheck
import { serverUrl } from '../config'

/**
 * @returns dswid from url query
 */
export function getDswid () {
  return window.dswh && window.dswh.windowId
    ? '?dswid=' + window.dswh.windowId
    : ''
}

/**
 * @returns string of project url (jsf)
 * e.g. https://translate.zanata.org/project/view/zanata-server?dswid=-805
 */
export function getProjectUrl (projectSlug) {
  return serverUrl + '/project/view/' + projectSlug + getDswid()
}

/**
 * @returns string of language page url (jsf)
 *
 */
export function getLanguageUrl (localeId) {
  return serverUrl + '/language/view/' + localeId
}

/**
 * @returns string of project version languages settings url (jsf)
 * e.g. https://translate.zanata.org/iteration/view/meikai/
 *      ver1/settings/languages?dswid=4384
 */
export function getVersionLanguageSettingsUrl (projectID, versionID) {
  return serverUrl + '/iteration/view/' + projectID + '/' + versionID +
    '/settings/languages' + getDswid()
}

/**
 *
 * @returns string of profile page url (react page)
 */
export function getProfileUrl (username) {
  return '/profile/view/' + username + getDswid()
}

/**
 *
 * @returns string of version group url (jsf)
 */
export function getVersionGroupUrl (slug) {
  return serverUrl + '/version-group/view/' + slug + getDswid()
}
