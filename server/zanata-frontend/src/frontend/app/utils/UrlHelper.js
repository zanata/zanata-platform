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
export function getProjectUrl (projectSlug) {
  return getContextPath() + '/project/view/' + projectSlug + getDswid()
}

export default {
  getDswid,
  getContextPath,
  getProjectUrl
}
