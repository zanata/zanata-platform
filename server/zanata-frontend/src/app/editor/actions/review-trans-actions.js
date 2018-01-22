import { CALL_API } from 'redux-api-middleware'
import { apiUrl } from '../../config'
import {
  getJsonHeaders,
  buildAPIRequest
} from '../../actions/common-actions'

export const ADD_REVIEW_REQUEST = 'ADD_REVIEW_REQUEST'
export const ADD_REVIEW_SUCCESS = 'ADD_REVIEW_SUCCESS'
export const ADD_REVIEW_FAILURE = 'ADD_REVIEW_FAILURE'

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
    transUnitId: 1,
    revision: 1,
    comment: 'bad',
    reviewCriteriaId: 1,
    status: 'Rejected'
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', getJsonHeaders(), apiTypes,
     JSON.stringify(body))
  }
}
