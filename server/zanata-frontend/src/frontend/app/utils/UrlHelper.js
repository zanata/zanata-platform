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
 * @returns string of project url
 * e.g. https://translate.zanata.org/project/view/zanata-server?dswid=-805
 */
export function getProjectUrl (project) {
  return serverUrl + '/project/view/' + project.id + getDswid()
}

/**
 * @returns string of language page url
 *
 */
export function getLanguageUrl (localeId) {
  return serverUrl + '/language/view/' + localeId
}

/**
 * @returns string of project version languages settings url
 * e.g. https://translate.zanata.org/iteration/view/meikai/
 *      ver1/settings/languages?dswid=4384
 */
export function getVersionLanguageSettingsUrl (projectID, versionID) {
  return serverUrl + '/iteration/view/' + projectID + '/' + versionID +
    '/settings/languages' + getDswid()
}

export default {
  getDswid,
  getProjectUrl
}
