/**
 * @returns dswid from url query
 */
export function getDswid () {
  return window.dswh && window.dswh.windowId
    ? '?dswid=' + window.dswh.windowId
    : ''
}

/**
 * @returns context path
 *
 * Should be /zanata or ''
 */
export function getContextPath () {
  return window.config.baseUrl || ''
}

/**
 * @returns string of project url
 * e.g. https://translate.zanata.org/project/view/zanata-server?dswid=-805
 */
export function getProjectUrl (project) {
  return getContextPath() + '/project/view/' + project.id + getDswid()
}

/**
 * @returns string of language page url
 *
 */
export function getLanguageUrl (localeId) {
  return window.config.baseUrl + '/language/view/' + localeId
}

export default {
  getDswid,
  getContextPath,
  getProjectUrl
}
