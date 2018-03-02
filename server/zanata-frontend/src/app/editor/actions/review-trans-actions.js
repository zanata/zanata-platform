import { CALL_API } from 'redux-api-middleware'
import { apiUrl } from '../../config'
import {
  getJsonHeaders,
  buildAPIRequest,
  // eslint-disable-next-line
  APITypes
} from '../../actions/common-actions'
import { savePhraseWithStatus } from './phrases-actions'
import { STATUS_REJECTED } from '../utils/status-util'
import { createAction } from 'redux-actions'

export const ADD_REVIEW_REQUEST = 'ADD_REVIEW_REQUEST'
export const ADD_REVIEW_SUCCESS = 'ADD_REVIEW_SUCCESS'
export const ADD_REVIEW_FAILURE = 'ADD_REVIEW_FAILURE'
export const GET_ALL_CRITERIA_REQUEST = 'GET_ALL_CRITERIA_REQUEST'
export const GET_ALL_CRITERIA_SUCCESS = 'GET_ALL_CRITERIA_SUCCESS'
export const GET_ALL_CRITERIA_FAILURE = 'GET_ALL_CRITERIA_FAILURE'

export const TOGGLE_REVIEW_MODAL = 'TOGGLE_REVIEW_MODAL'

/**
 * Perform a save of a translation review with the given review data
 */
export function rejectTranslation (dispatch, review) {
  const endpoint = `${apiUrl}/review/trans/${review.localeId}`
  /** @type {APITypes} */
  const apiTypes = [
    ADD_REVIEW_REQUEST,
    {
      type: ADD_REVIEW_SUCCESS,
      payload: (_action, _state, res) => {
        return res.json().then((json) => {
          dispatch(savePhraseWithStatus(
            review.phrase, STATUS_REJECTED, review.reviewComment))
          return json
        })
      }
    },
    ADD_REVIEW_FAILURE]
  const body = {
    transUnitId: review.transUnitId,
    revision: review.revision,
    comment: review.reviewComment,
    reviewCriteriaId: review.criteriaId,
    status: 'Rejected'
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', getJsonHeaders(), apiTypes,
     JSON.stringify(body))
  }
}

/**
 * Perform a save of a review comment for a given TransUnit
 */
export function postReviewComment (review) {
  const endpoint = `${apiUrl}/review/trans/${review.localeId}`
  const apiTypes = [
    ADD_REVIEW_REQUEST,
    ADD_REVIEW_SUCCESS,
    ADD_REVIEW_FAILURE]
  const body = {
    transUnitId: review.transUnitId,
    revision: review.revision,
    comment: review.reviewComment,
    reviewCriteriaId: review.criteriaId,
    status: review.status
  }
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'PUT', getJsonHeaders(), apiTypes,
     JSON.stringify(body))
  }
}

export const toggleReviewModal = createAction(TOGGLE_REVIEW_MODAL)

export function fetchAllCriteria () {
  const endpoint = `${apiUrl}/review`
  /** @type {APITypes} */
  const apiTypes = [
    GET_ALL_CRITERIA_REQUEST,
    GET_ALL_CRITERIA_SUCCESS,
    GET_ALL_CRITERIA_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}
