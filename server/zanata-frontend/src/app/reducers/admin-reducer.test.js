// @ts-nocheck
/* global jest describe it expect */

import adminReducer from './admin-reducer'
import update from 'immutability-helper'
import { keyBy } from 'lodash'

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
import {
  GET_SERVER_SETTINGS_FAILURE,
  GET_SERVER_SETTINGS_REQUEST,
  GET_SERVER_SETTINGS_SUCCESS,
  SAVE_SERVER_SETTINGS_FAILURE,
  SAVE_SERVER_SETTINGS_REQUEST,
  SAVE_SERVER_SETTINGS_SUCCESS
} from '../actions/admin-actions'

describe('admin-reducer test', () => {
  it('can save server settings request', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const getAll = adminReducer(initial, {
      type: SAVE_SERVER_SETTINGS_REQUEST,
      payload: undefined
    })

    expect(initial.serverSettings.saving).toEqual(false)
    expect(getAll.serverSettings.saving).toEqual(true)
  })

  it('can save server settings success', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const settings = [
      {
        key: 'host.url',
        value: 'http://localhost.com',
        defaultValue: ''
      },
      {
        key: 'term.url',
        value: 'http://localhost.com/terms',
        defaultValue: ''
      }
    ]
    const result = keyBy(settings, o => o.key)
    const getAll = adminReducer(initial, {
      type: SAVE_SERVER_SETTINGS_SUCCESS,
      payload: settings
    })

    expect(getAll.serverSettings.settings).toEqual(result)
    expect(getAll.serverSettings.saving).toEqual(false)
  })

  it('can save server settings failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: SAVE_SERVER_SETTINGS_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.serverSettings.saving).toEqual(false)
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
        .toEqual({
          duration: null,
          description: '401 - Unauthorized',
          message: 'Failed to save settings.' +
          'Please refresh this page and try again',
          severity: 'error' })
  })

  it('can fetch server settings request', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const getAll = adminReducer(initial, {
      type: GET_SERVER_SETTINGS_REQUEST,
      payload: undefined
    })

    expect(initial.serverSettings.loading).toEqual(false)
    expect(getAll.serverSettings.loading).toEqual(true)
  })

  it('can fetch server settings', () => {
    const initial = adminReducer(undefined, { type: 'any' })
    const settings = [
      {
        key: 'host.url',
        value: 'http://localhost.com',
        defaultValue: ''
      },
      {
        key: 'term.url',
        value: 'http://localhost.com/terms',
        defaultValue: ''
      }
    ]
    const result = keyBy(settings, o => o.key)
    const getAll = adminReducer(initial, {
      type: GET_SERVER_SETTINGS_SUCCESS,
      payload: settings
    })

    expect(initial.serverSettings.settings).toEqual({})
    expect(initial.serverSettings.loading).toEqual(false)
    expect(getAll.serverSettings.settings).toEqual(result)
  })

  it('can fetch server settings failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: GET_SERVER_SETTINGS_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.serverSettings.loading).toEqual(false)
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
        .toEqual({
          duration: null,
          description: '401 - Unauthorized',
          message: 'Unable to get server settings.' +
          'Please refresh this page and try again',
          severity: 'error' })
  })

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

  it('can handle fetch all criteria failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: GET_ALL_CRITERIA_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual({
        duration: null,
        description: '401 - Unauthorized',
        message: 'Failed to retrieve review criteria.',
        severity: 'error' })
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
      .toEqual({
        duration: null,
        description: '401 - Unauthorized',
        message: 'Add Criteria failed.',
        severity: 'error'
      })
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
      .toEqual({
        duration: null,
        description: '401 - Unauthorized',
        message: 'Edit criteria failed.',
        severity: 'error'
      })
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
  it('can handle delete criterion failure', () => {
    const initial = adminReducer(undefined, { type: 'any' })

    const failed = adminReducer(initial, {
      type: DELETE_CRITERION_FAILURE,
      error: true,
      payload: {
        message: '401 - Unauthorized'
      }})
    expect(initial.notification).toBeUndefined()
    expect(failed.notification)
      .toEqual({
        duration: null,
        description: '401 - Unauthorized',
        message: 'Delete Criteria failed.',
        severity: 'error'
      })
  })
})
