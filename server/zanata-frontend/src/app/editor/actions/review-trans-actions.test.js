import { CALL_API } from 'redux-api-middleware'
import { apiUrl } from '../../config'
import { rejectTranslation, fetchAllCriteria } from './review-trans-actions'

export const ADD_REVIEW_REQUEST = 'ADD_REVIEW_REQUEST'
export const ADD_REVIEW_SUCCESS = 'ADD_REVIEW_SUCCESS'
export const ADD_REVIEW_FAILURE = 'ADD_REVIEW_FAILURE'
export const GET_ALL_CRITERIA_REQUEST = 'GET_ALL_CRITERIA_REQUEST'
export const GET_ALL_CRITERIA_SUCCESS = 'GET_ALL_CRITERIA_SUCCESS'
export const GET_ALL_CRITERIA_FAILURE = 'GET_ALL_CRITERIA_FAILURE'

/* global describe it expect */
describe('review-trans-actions', () => {
  it('can fetch all criteria', () => {
    const apiAction = fetchAllCriteria()
    expect(apiAction[CALL_API].endpoint).toEqual(
      apiUrl + '/review'
    )
  })
  it('can add a new review', () => {
    const review = {
      transUnitId: 0,
      localeId: 'ja',
      revision: 1,
      selectedPriority: 'Minor',
      priorityId: 0,
      selectedCriteria: '',
      criteriaId: 1,
      reviewComment: ''
    }
    const apiAction = rejectTranslation(undefined, review)
    expect(apiAction[CALL_API].endpoint).toEqual(
      apiUrl + '/review/trans/ja'
    )
  })
})
