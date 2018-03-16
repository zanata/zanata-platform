import { isEmpty } from 'lodash'

/**
 * @param {string} str
 */
export function trimLeadingSpace (str) {
  return isEmpty(str) ? str : str.replace(/^\s+/g, '')
}
/**
 * @param {string} str
 */
export function trim (str) {
  return isEmpty(str) ? str : str.trim()
}
/**
 * @param {string} str
 */
export function isJsonString (str) {
  try {
    JSON.parse(str)
  } catch (e) {
    return false
  }
  return true
}

export default {
  trimLeadingSpace,
  trim,
  isJsonString
}
