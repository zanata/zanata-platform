jest.disableAutomock()

import versionReducer from './version-reducer'

import {
  TOGGLE_TM_MERGE_MODAL,
  // VERSION_LOCALES_SUCCESS,
  // VERSION_LOCALES_FAILURE,
  // PROJECT_PAGE_SUCCESS,
  // PROJECT_PAGE_FAILURE
} from '../actions/version-action-types'

describe('version-reducer test', () => {
  it('can toggle the merge modal', () => {
    const initial = versionReducer(undefined, { type: 'any' })
    const shown = versionReducer(initial, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: true }
    })
    const hidden = versionReducer(shown, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: false }
    })
    expect(initial.TMMerge).toEqual(
      {projectVersions: [], show: false}
    )
    expect(shown.TMMerge).toEqual(
      {projectVersions: [], show: true}
    )
    expect(hidden.TMMerge).toEqual(
      {projectVersions: [], show: false}
    )
  })
})