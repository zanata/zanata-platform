// @ts-nocheck
/* global jest describe it expect */

import adminReducer from './admin-reducer'
import update from 'immutability-helper'

import {
  GET_ALL_CRITERIA_SUCCESS,
  ADD_CRITERION_SUCCESS,
  ADD_CRITERION_FAILURE,
  EDIT_CRITERION_SUCCESS,
  EDIT_CRITERION_FAILURE,
  DELETE_CRITERION_SUCCESS,
  DELETE_CRITERION_FAILURE,
  GET_ALL_CRITERIA_FAILURE
} from '../actions/review-actions'

describe('admin-reducer test', () => {
  it('can fetch all criteria', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const criteria = [{
      id: 1,
      priority: 'Major',
      description: 'bad grammar',
      commentRequired: false
    }]
    const getAll = adminReducer(initial, {
      type: GET_ALL_CRITERIA_SUCCESS,
      payload: criteria
    })

    expect(initial.review.criteria).toEqual([])
    expect(getAll.review.criteria).toEqual(criteria)
  })

  it('can handle fetch all failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: GET_ALL_CRITERIA_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual('Failed to retrieve review criteria. 401 - Unauthorized')
  })

  it('can add new criterion', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const criteria = {
      id: 1,
      priority: 'Major',
      description: 'bad grammar',
      commentRequired: false
    }
    const added = adminReducer(initial, {
      type: ADD_CRITERION_SUCCESS,
      payload: criteria
    })

    expect(initial.review.criteria).toEqual([])
    expect(added.review.criteria).toEqual([criteria])
  })
  it('can handle add new criterion failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: ADD_CRITERION_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual('Operation failed. 401 - Unauthorized')
  })

  it('can edit criterion', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const criteria = {
      id: 1,
      priority: 'Major',
      description: 'bad grammar',
      commentRequired: false
    }
    const updatedCriteria = update(criteria,
      {description: {$set: 'new reason'}})
    const withSomething = adminReducer(initial, {
      type: ADD_CRITERION_SUCCESS,
      payload: criteria
    })
    const updated = adminReducer(withSomething, {
      type: EDIT_CRITERION_SUCCESS,
      payload: updatedCriteria
    })

    expect(initial.review.criteria).toEqual([])
    expect(updated.review.criteria).toEqual([updatedCriteria])
  })
  it('can handle edit criterion failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: EDIT_CRITERION_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual('Operation failed. 401 - Unauthorized')
  })

  it('can delete criterion', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const criteria = {
      id: 1,
      priority: 'Major',
      description: 'bad grammar',
      commentRequired: false
    }
    const withSomething = adminReducer(initial, {
      type: ADD_CRITERION_SUCCESS,
      payload: criteria
    })
    const deleted = adminReducer(withSomething, {
      type: DELETE_CRITERION_SUCCESS,
      payload: criteria
    })

    expect(initial.review.criteria).toEqual([])
    expect(deleted.review.criteria).toEqual([])
  })
  it('can handle edit criterion failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: DELETE_CRITERION_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual('Operation failed. 401 - Unauthorized')
  })
})
