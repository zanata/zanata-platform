// @ts-nocheck
import review from './review-trans-reducer'

import {
  GET_ALL_CRITERIA_SUCCESS,
  GET_ALL_CRITERIA_FAILURE
} from '../actions/review-trans-actions'

/* global describe it expect */
describe('review-trans-reducer', () => {
  it('generates initial state', () => {
    expect(review(undefined, {})).toEqual({
      notification: undefined,
      criteria: [],
      showReviewModal: false
    })
  })
  it('can recieve criteria', () => {
    expect(review(undefined, {
      type: GET_ALL_CRITERIA_SUCCESS,
      payload: [{
        id: 1,
        commentRequired: true,
        description: 'invariable invariant variable',
        priority: 'Critical'
      }]
    })).toEqual({
      notification: undefined,
      criteria: [
        {commentRequired: true,
        description: 'Unspecified Criteria',
        id: undefined,
        priority: 'Minor'},
        {commentRequired: true,
        description: 'invariable invariant variable',
        id: 1,
        priority: 'Critical'
      }],
      showReviewModal: false
    })
  })
  it('can handle get criteria failure', () => {
    expect(review(undefined, {
      type: GET_ALL_CRITERIA_FAILURE,
      payload: 'text'
    })).toEqual({
      notification: 'Failed to retrieve review criteria. undefined',
      criteria: [],
      showReviewModal: false
    })
  })
})
