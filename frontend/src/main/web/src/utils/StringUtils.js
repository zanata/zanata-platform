import { isEmpty } from 'lodash'

export function trimLeadingSpace (str) {
  return isEmpty(str) ? str : str.replace(/^\s+/g, '')
}
export function trim (str) {
  return isEmpty(str) ? str : str.trim()
}

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
