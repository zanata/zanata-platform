import { CALL_API } from 'redux-api-middleware'
import { apiUrl } from '../../config'
import {
  getJsonHeaders,
  buildAPIRequest
} from '../../actions/common-actions'

export const ADD_REVIEW_REQUEST = 'ADD_REVIEW_REQUEST'
export const ADD_REVIEW_SUCCESS = 'ADD_REVIEW_SUCCESS'
export const ADD_REVIEW_FAILURE = 'ADD_REVIEW_FAILURE'
export const GET_ALL_CRITERIA_REQUEST = 'GET_ALL_CRITERIA_REQUEST'
export const GET_ALL_CRITERIA_SUCCESS = 'GET_ALL_CRITERIA_SUCCESS'
export const GET_ALL_CRITERIA_FAILURE = 'GET_ALL_CRITERIA_FAILURE'

/**
 * Perform a save with the given info, and recursively start next save if
 * one has queued when the save finishes.
 */
export function addNewReview (review, localeId) {
  const endpoint = `${apiUrl}/review/trans/${localeId}`
  const apiTypes = [
    ADD_REVIEW_REQUEST,
    ADD_REVIEW_SUCCESS,
    ADD_REVIEW_FAILURE]
  const body = {
    transUnitId: 154,
    revision: 1,
    comment: review.reviewComment,
    reviewCriteriaId: review.criteriaId,
    status: 'Rejected'
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', getJsonHeaders(), apiTypes,
     JSON.stringify(body))
  }
}

export function fetchAllCriteria () {
  const endpoint = `${apiUrl}/review`
  const apiTypes = [
    GET_ALL_CRITERIA_REQUEST,
    GET_ALL_CRITERIA_SUCCESS,
    GET_ALL_CRITERIA_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}
