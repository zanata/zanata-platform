// @ts-check
import { createAction } from 'redux-actions'
import { auth } from '../config'

export const CLEAR_MESSAGE = 'CLEAR_MESSAGE'
export const clearMessage = createAction(CLEAR_MESSAGE)

export const LOAD_USER_REQUEST = 'LOAD_USER_REQUEST'
export const LOAD_USER_SUCCESS = 'LOAD_USER_SUCCESS'
export const LOAD_USER_FAILURE = 'LOAD_USER_FAILURE'

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
  if (auth) {
    headers['x-auth-token'] = auth.token
    headers['x-auth-user'] = auth.user
  }
  return headers
}

export const getJsonHeaders = () => {
  let headers = getHeaders()
  headers['Accept'] = 'application/json'
  headers['Content-Type'] = 'application/json'
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
