/* global jest describe it expect */

import versionReducer from './version-reducer'

import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_REQUEST,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_REQUEST,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE,
  VERSION_TM_MERGE_REQUEST,
  VERSION_TM_MERGE_SUCCESS,
  VERSION_TM_MERGE_FAILURE,
  TM_MERGE_CANCEL_REQUEST,
  TM_MERGE_CANCEL_SUCCESS,
  QUERY_TM_MERGE_PROGRESS_REQUEST,
  QUERY_TM_MERGE_PROGRESS_FAILURE,
  TM_MERGE_PROCESS_FINISHED
} from '../actions/version-action-types'

describe('version-reducer test', () => {
  it('can toggle the merge modal', () => {
    const initial = versionReducer(undefined, { type: 'any' })
    // @ts-ignore
    const shown = versionReducer(initial, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: true }
    })
    // @ts-ignore
    const hidden = versionReducer(shown, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: false }
    })
    expect(initial.TMMerge).toEqual(
      {processStatus: undefined, queryStatus: undefined,
        projectVersions: [], show: false, triggered: false}
    )
    expect(shown.TMMerge).toEqual(
      {processStatus: undefined, queryStatus: undefined,
        projectVersions: [], show: true, triggered: false}
    )
    expect(hidden.TMMerge).toEqual(
      {processStatus: undefined, queryStatus: undefined,
        projectVersions: [], show: false, triggered: false}
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
    const localeSuccessAction = {
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
    const localesRequested = versionReducer(initial, requestAction)
    // @ts-ignore
    const localesReceived = versionReducer(localesRequested, localeSuccessAction)

    expect(localesRequested.fetchingLocale).toEqual(true)
    expect(localesReceived.fetchingLocale).toEqual(false)
    expect(localesReceived.locales).toEqual([{
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
    const timestamp = Date.now()
    const requestAction = {
      type: PROJECT_PAGE_REQUEST
    }
    const projectSuccessAction = {
      type: PROJECT_PAGE_SUCCESS,
      meta: {timestamp},
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
    const projectsRequested = versionReducer(initial, requestAction)
    const projectsReceived = versionReducer(projectsRequested,
      // @ts-ignore
      projectSuccessAction)
    expect(projectsRequested.fetchingProject).toEqual(true)
    expect(projectsReceived.fetchingProject).toEqual(false)
    expect(projectsReceived.TMMerge.projectVersions).toEqual(
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

  it('does not use stale results', () => {
    const stalePayload = [{
      contributorCount: 0,
      description: 'A stale project',
      id: 'meikai1',
      status: 'ACTIVE',
      title: 'Meikai1',
      type: 'Project',
      versions: [
        {
          id: 'ver1',
          status: 'ACTIVE'
        }
      ]
    }]
    const freshPayload = [{
      contributorCount: 0,
      description: 'A fresh project',
      id: 'meikai2',
      status: 'ACTIVE',
      title: 'Meikai2',
      type: 'Project',
      versions: [
        {
          id: 'ver1',
          status: 'ACTIVE'
        }
      ]
    }]
    const brunch = new Date(2017, 4, 4, 11, 0, 0, 0)
    const highTea = new Date(2017, 4, 4, 12, 0, 0, 3)
    const firstProjectSuccessAction = {
      type: PROJECT_PAGE_SUCCESS,
      meta: { timestamp: brunch },
      payload: stalePayload
    }
    const secondProjectSuccessAction = {
      type: PROJECT_PAGE_SUCCESS,
      meta: { timestamp: highTea },
      payload: freshPayload
    }
    // The project result with the most recent timestamp should be maintained
    const newestResults = versionReducer(undefined,
      // @ts-ignore
      secondProjectSuccessAction)
    const withStaleAction = versionReducer(newestResults,
      // @ts-ignore
      firstProjectSuccessAction)

    expect(withStaleAction.projectResultsTimestamp).toEqual(highTea)
    expect(withStaleAction.TMMerge.projectVersions[0])
      .toEqual(newestResults.TMMerge.projectVersions[0])
  })
  it('can request a TM merge', () => {
    const requestAction = {
      type: VERSION_TM_MERGE_REQUEST
    }
    const mergeSuccessAction = {
      type: VERSION_TM_MERGE_SUCCESS,
      payload: {
        messages: [],
        percentageComplete: 100,
        statusCode: 'Running',
        url: 'http://localhost:8080/rest/process/key/TMMergeForVerKey-1-ja'
      }
    }
    const initial = versionReducer(undefined, {type: 'any'})
    const mergeRequested = versionReducer(initial, requestAction)
    // @ts-ignore
    const mergeReceived = versionReducer(mergeRequested, mergeSuccessAction)
    expect(mergeReceived).toEqual({
      TMMerge: {
        processStatus: {
          messages: [],
          percentageComplete: 100,
          statusCode: 'Running',
          url: 'http://localhost:8080/rest/process/key/TMMergeForVerKey-1-ja'
        },
        projectVersions: [],
        show: false,
        triggered: false
      },
      fetchingLocale: false,
      fetchingProject: false,
      locales: [],
      notification: undefined,
      projectResultsTimestamp: initial.projectResultsTimestamp
    })
  })
  it('can track TM merge progress', () => {
    const initial = versionReducer(undefined, { type: 'any' })
    const requestAction = versionReducer(initial, {
      type: VERSION_TM_MERGE_REQUEST
    })
    expect(requestAction.TMMerge.triggered).toEqual(true)
    const failed = versionReducer(requestAction, {
      type: VERSION_TM_MERGE_FAILURE
    })
    expect(failed.TMMerge.processStatus).toEqual(undefined)
  })

  it('can cancel TM merge requests', () => {
    const requestAction = {
      type: TM_MERGE_CANCEL_REQUEST
    }
    const cancelSuccessAction = {
      type: TM_MERGE_CANCEL_SUCCESS
    }
    const initial = versionReducer(undefined, {type: 'any'})
    const cancelRequested = versionReducer(initial, requestAction)
    const cancelReceived = versionReducer(cancelRequested, cancelSuccessAction)
    expect(cancelReceived).toEqual({
      TMMerge: {
        processStatus: undefined,
        projectVersions: [],
        show: false,
        triggered: false
      },
      fetchingLocale: false,
      fetchingProject: false,
      locales: [],
      notification: undefined,
      projectResultsTimestamp: initial.projectResultsTimestamp
    })
  })
  it('can Query TM merge progress', () => {
    const initial = versionReducer(undefined, {type: 'any'})
    const queryRequestAction = {
      type: QUERY_TM_MERGE_PROGRESS_REQUEST
    }
    const queryFailureAction = {
      type: QUERY_TM_MERGE_PROGRESS_FAILURE
    }
    const queryRequested = versionReducer(initial, queryRequestAction)
    const failureRecieved = versionReducer(queryRequested, queryFailureAction)
    expect(failureRecieved).toEqual({
      TMMerge: {
        processStatus: undefined,
        projectVersions: [],
        show: false,
        triggered: false
      },
      fetchingLocale: false,
      fetchingProject: false,
      locales: [],
      notification: undefined,
      projectResultsTimestamp: initial.projectResultsTimestamp
    })
  })
  it('can handle TM Merge completion', () => {
    const initial = versionReducer(undefined, {type: 'any'})
    const queryRequestAction = {
      type: TM_MERGE_PROCESS_FINISHED
    }
    const statusRequested = versionReducer(initial, queryRequestAction)
    expect(statusRequested.TMMerge.processStatus).toEqual(undefined)
  })
})
