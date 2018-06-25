import { createAction } from 'redux-actions'
import { auth } from '../config'
import { HTTPVerb } from 'redux-api-middleware';

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
  localeId: 'en-US',
  displayName: 'English (United States)'
}

interface Headers {[k: string]: string}

export const getHeaders = () => {
  const headers: Headers = {}
  if (auth) {
    headers['x-auth-token'] = auth.token
    headers['x-auth-user'] = auth.user
  }
  return headers
}

export const getJsonHeaders = () => {
  const headers = getHeaders()
  headers.Accept = 'application/json'
  headers['Content-Type'] = 'application/json'
  return headers
}

/** Basically just a tuple of length 3 */
export type APITypes = [string|{}, string|{}, string|{}]

// derived from RSAAction in redux-api-middleware.d.ts
export interface APIRequest {
    endpoint: string;  // or function
    method: HTTPVerb;
    body?: any;
    headers?: { [propName: string]: string }; // or function
    credentials?: 'omit' | 'same-origin' | 'include';
    bailout?: boolean; // or function
    types: APITypes;
}

export const buildAPIRequest = (endpoint: string, method: HTTPVerb, headers: Headers,
    types: APITypes, body?: any) => {
  const result: APIRequest = {
    endpoint,
    method,
    headers,
    credentials: 'include',
    types,
  }

  if (body) {
    result.body = body
  }

  return result
}
