import { createAction } from 'redux-actions'

export const CLEAR_MESSAGE = 'CLEAR_MESSAGE'
export const clearMessage = createAction(CLEAR_MESSAGE)

export const SEVERITY = {
  INFO: 'info',
  WARN: 'warn',
  ERROR: 'error'
}

export const DEFAULT_LOCALE = {
  'localeId': 'en-US',
  'displayName': 'English (United States)'
}

export const getHeaders = () => {
  let headers = {}
  if (window.config.auth) {
    headers['x-auth-token'] = window.config.auth.token
    headers['x-auth-user'] = window.config.auth.user
  }
  return headers
}

export const getJsonHeaders = () => {
  let headers = getHeaders()
  headers['Accept'] = 'application/json'
  return headers
}

export const buildAPIRequest = (endpoint, method, headers, types, body) => {
  let result = {
    endpoint,
    method,
    headers,
    credentials: 'include',
    types
  }

  if (body) {
    result.body = body
  }
  return result
}
