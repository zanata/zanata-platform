// import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import {
  getJsonHeaders,
  buildAPIRequest,
  APITypes
} from './common-actions'
import { apiUrl } from '../config'

export const GET_ALL_CRITERIA_REQUEST = 'GET_ALL_CRITERIA_REQUEST'
export const GET_ALL_CRITERIA_SUCCESS = 'GET_ALL_CRITERIA_SUCCESS'
export const GET_ALL_CRITERIA_FAILURE = 'GET_ALL_CRITERIA_FAILURE'

export function fetchAllCriteria () {
  const endpoint = `${apiUrl}/review`
  const apiTypes: APITypes = [
    GET_ALL_CRITERIA_REQUEST,
    GET_ALL_CRITERIA_SUCCESS,
    GET_ALL_CRITERIA_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export const ADD_CRITERION_REQUEST = 'ADD_CRITERION_REQUEST'
export const ADD_CRITERION_SUCCESS = 'ADD_CRITERION_SUCCESS'
export const ADD_CRITERION_FAILURE = 'ADD_CRITERION_FAILURE'

// @ts-ignore any
export function addNewCriterion (criterion) {
  const endpoint = `${apiUrl}/review/criteria`
  const apiTypes: APITypes = [
    ADD_CRITERION_REQUEST,
    ADD_CRITERION_SUCCESS,
    ADD_CRITERION_FAILURE]
  const body = {
    ...criterion,
    commentRequired: criterion.isCommentRequired
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'POST', getJsonHeaders(), apiTypes,
      JSON.stringify(body))
  }
}

export const DELETE_CRITERION_REQUEST = 'DELETE_CRITERION_REQUEST'
export const DELETE_CRITERION_SUCCESS = 'DELETE_CRITERION_SUCCESS'
export const DELETE_CRITERION_FAILURE = 'DELETE_CRITERION_FAILURE'
// @ts-ignore any
export function removeCriterion (id) {
  const endpoint = `${apiUrl}/review/criteria/${id}`
  const types: APITypes = [DELETE_CRITERION_REQUEST, DELETE_CRITERION_SUCCESS,
    DELETE_CRITERION_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'DELETE', getJsonHeaders(), types)
  }
}

export const EDIT_CRITERION_REQUEST = 'EDIT_CRITERION_REQUEST'
export const EDIT_CRITERION_SUCCESS = 'EDIT_CRITERION_SUCCESS'
export const EDIT_CRITERION_FAILURE = 'EDIT_CRITERION_FAILURE'

// @ts-ignore any
export function editCriterion (criterion) {
  const types: APITypes = [EDIT_CRITERION_REQUEST, EDIT_CRITERION_SUCCESS,
    EDIT_CRITERION_FAILURE]
  const endpoint = `${apiUrl}/review/criteria/${criterion.id}`
  const body = {
    ...criterion,
    commentRequired: criterion.isCommentRequired
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', getJsonHeaders(), types,
      JSON.stringify(body))
  }
}
