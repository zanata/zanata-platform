/* global jest describe it expect */

import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from '../../actions/phrases-action-types'
import filterStatusReducer from './filter-status-reducer'

describe('filter-status-reducer test', () => {
  it('can reset filters', () => {
    const onlyApproved = filterStatusReducer(undefined, {
      type: UPDATE_STATUS_FILTER,
      payload: 'approved'
    })
    const approvedAndTranslated = filterStatusReducer(onlyApproved, {
      type: UPDATE_STATUS_FILTER,
      payload: 'translated'
    })
    const filterReset = filterStatusReducer(approvedAndTranslated, {
      type: RESET_STATUS_FILTERS
    })
    expect(filterReset).toEqual({
      all: true,
      approved: false,
      rejected: false,
      translated: false,
      needswork: false,
      untranslated: false
    })
  })

  it('can update filters', () => {
    const onlyApproved = filterStatusReducer(undefined, {
      type: UPDATE_STATUS_FILTER,
      payload: 'approved'
    })
    const approvedAndTranslated = filterStatusReducer(onlyApproved, {
      type: UPDATE_STATUS_FILTER,
      payload: 'translated'
    })
    const includeNeedsWork = filterStatusReducer(approvedAndTranslated, {
      type: UPDATE_STATUS_FILTER,
      payload: 'needswork'
    })
    const excludeNeedsWork = filterStatusReducer(includeNeedsWork, {
      type: UPDATE_STATUS_FILTER,
      payload: 'needswork'
    })
    const includeRejected = filterStatusReducer(excludeNeedsWork, {
      type: UPDATE_STATUS_FILTER,
      payload: 'rejected'
    })
    const includeUntranslated = filterStatusReducer(includeRejected, {
      type: UPDATE_STATUS_FILTER,
      payload: 'untranslated'
    })
    const includeAll = filterStatusReducer(includeUntranslated, {
      type: UPDATE_STATUS_FILTER,
      payload: 'needswork'
    })

    expect(onlyApproved).toEqual({
      all: false,
      approved: true,
      rejected: false,
      translated: false,
      needswork: false,
      untranslated: false
    })
    expect(approvedAndTranslated).toEqual({
      all: false,
      approved: true,
      rejected: false,
      translated: true,
      needswork: false,
      untranslated: false
    })
    expect(includeNeedsWork).toEqual({
      all: false,
      approved: true,
      rejected: false,
      translated: true,
      needswork: true,
      untranslated: false
    })
    expect(excludeNeedsWork).toEqual({
      all: false,
      approved: true,
      rejected: false,
      translated: true,
      needswork: false,
      untranslated: false
    })
    expect(includeRejected).toEqual({
      all: false,
      approved: true,
      rejected: true,
      translated: true,
      needswork: false,
      untranslated: false
    })
    expect(includeUntranslated).toEqual({
      all: false,
      approved: true,
      rejected: true,
      translated: true,
      needswork: false,
      untranslated: true
    })
    expect(includeAll).toEqual({
      all: true,
      approved: false,
      rejected: false,
      translated: false,
      needswork: false,
      untranslated: false
    })
  })
})
