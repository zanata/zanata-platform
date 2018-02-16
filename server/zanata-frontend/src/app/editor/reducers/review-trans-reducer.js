import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import {
  GET_ALL_CRITERIA_SUCCESS,
  GET_ALL_CRITERIA_FAILURE,
  TOGGLE_REVIEW_MODAL
} from '../actions/review-trans-actions'

const defaultState = {
  notification: undefined,
  showReviewModal: false,
  criteria: []
}

// selectors
export const getCriteria = state => state.review.criteria

// utility function
const getErrorMessage = action => {
  if (action.error) {
    return action.payload && action.payload.message
  }
  return undefined
}

const review = handleActions({
  [GET_ALL_CRITERIA_SUCCESS]: (state, action) => {
    return update(state, {
      criteria: { $set: action.payload }
    })
  },
  [GET_ALL_CRITERIA_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: `Failed to retrieve review criteria. ${getErrorMessage(action)}`
      }
    })
  },
  [TOGGLE_REVIEW_MODAL]: (state, _action) => {
    return update(state, {
      showReviewModal: {
        $set: !state.showReviewModal
      }
    })
  }
}, defaultState)

export default review
