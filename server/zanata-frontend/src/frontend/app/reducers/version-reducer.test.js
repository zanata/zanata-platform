jest.disableAutomock()

import versionReducer from './version-reducer'

import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_REQUEST,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_REQUEST,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE
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

  it('can track fetching locales', () => {
    const initial = versionReducer(undefined, { type: 'any' })
    const requested = versionReducer(initial, {
      type: VERSION_LOCALES_REQUEST
    })
    expect(requested.fetchingLocale).toEqual(true)
    const failed = versionReducer(requested, {
      type: VERSION_LOCALES_FAILURE
    })
    expect(failed.fetchingLocale).toEqual(false)
  })

  it('can receive locales', () => {
    const requestAction = {
      type: VERSION_LOCALES_REQUEST
    }
    const details = {
      type: VERSION_LOCALES_SUCCESS,
      payload:
        [{
          displayName: 'Japanese',
          localeId: 'ja',
          nativeName: '日本語'
        },
        {
          displayName: 'Azerbaijani',
          localeId: 'az',
          nativeName: 'azərbaycan dili'
        }]
    }
    const initial = versionReducer(undefined, {type: 'any'})
    const withFirstRequest = versionReducer(initial, requestAction)
    const withFirst = versionReducer(withFirstRequest, details)

    expect(withFirstRequest.fetchingLocale).toEqual(true)
    expect(withFirst.fetchingLocale).toEqual(false)
    expect(withFirst.locales).toEqual([{
        displayName: 'Japanese',
        localeId: 'ja',
        nativeName: '日本語'
      },
      {
        displayName: 'Azerbaijani',
        localeId: 'az',
        nativeName: 'azərbaycan dili'
      }])
  })

  it('can track fetching projects', () => {
    const initial = versionReducer(undefined, { type: 'any' })
    const requested = versionReducer(initial, {
      type: PROJECT_PAGE_REQUEST
    })
    expect(requested.fetchingProject).toEqual(true)
    const failed = versionReducer(requested, {
      type: PROJECT_PAGE_FAILURE
    })
    expect(failed.fetchingProject).toEqual(false)
  })

  it('can receive projects', () => {
    const requestAction = {
      type: PROJECT_PAGE_REQUEST
    }
    const details = {
      type: PROJECT_PAGE_SUCCESS,
      payload:
        [{
          contributorCount: 0,
          description: 'A project',
          id: 'meikai',
          status: 'ACTIVE',
          title: 'Meikai',
          type: 'Project',
          versions: [
            {
              id: 'ver1',
              status: 'ACTIVE'
            }
          ]
        },
        {
          contributorCount: 0,
          description: 'Locked project',
          id: 'meikailocked',
          status: 'READONLY',
          title: 'MeikaiLocked',
          type: 'Project',
          versions: [
            {
              id: 'ver1',
              status: 'READONLY'
            }
          ]
        }]
    }
    const initial = versionReducer(undefined, {type: 'any'})
    const withFirstRequest = versionReducer(initial, requestAction)
    const withFirst = versionReducer(withFirstRequest, details)

    expect(withFirstRequest.fetchingProject).toEqual(true)
    expect(withFirst.fetchingProject).toEqual(false)
    expect(withFirst.TMMerge.projectVersions).toEqual(
      [{
        contributorCount: 0,
        description: 'A project',
        id: 'meikai',
        status: 'ACTIVE',
        title: 'Meikai',
        type: 'Project',
        versions: [
          {
            id: 'ver1',
            status: 'ACTIVE'
          }
        ]
      },
        {
          contributorCount: 0,
          description: 'Locked project',
          id: 'meikailocked',
          status: 'READONLY',
          title: 'MeikaiLocked',
          type: 'Project',
          versions: [
            {
              id: 'ver1',
              status: 'READONLY'
            }
          ]
        }]
    )
  })
})
