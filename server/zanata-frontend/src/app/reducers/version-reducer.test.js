/* global jest describe it expect */

// import versionReducer, {defaultState} from './version-reducer'
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
/** @typedef {import('./state').ProjectVersionState} ProjectVersionState */

// @ts-ignore Need Redux 4: https://github.com/reactjs/redux/pull/2773
const undefState = /** @type {ProjectVersionState} */ (undefined)
const dummyAction = { type: 'any' }

describe('version-reducer test', () => {
  it('can toggle the merge modal', () => {
    const initial = versionReducer(undefState, dummyAction)
    const shown = versionReducer(initial, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: true }
    })
    const hidden = versionReducer(shown, {
      type: TOGGLE_TM_MERGE_MODAL,
      payload: { show: false }
    })
    expect(initial.TMMerge).toEqual({
      processStatus: undefined,
      queryStatus: undefined,
      projectsWithVersions: [],
      show: false,
      triggered: false
    })
    expect(shown.TMMerge).toEqual({
      processStatus: undefined,
      queryStatus: undefined,
      projectsWithVersions: [],
      show: true,
      triggered: false
    })
    expect(hidden.TMMerge).toEqual({
      processStatus: undefined,
      queryStatus: undefined,
      projectsWithVersions: [],
      show: false,
      triggered: false
    })
  })

  it('can track fetching locales', () => {
    const initial = versionReducer(undefState, dummyAction)
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
      payload: [
        {
          displayName: 'Japanese',
          localeId: 'ja',
          nativeName: '日本語'
        },
        {
          displayName: 'Azerbaijani',
          localeId: 'az',
          nativeName: 'azərbaycan dili'
        }
      ]
    }
    const initial = versionReducer(undefState, dummyAction)
    const localesRequested = versionReducer(initial, requestAction)
    // @ts-ignore
    const localesReceived = versionReducer(
      localesRequested,
      localeSuccessAction
    )

    expect(localesRequested.fetchingLocale).toEqual(true)
    expect(localesReceived.fetchingLocale).toEqual(false)
    expect(localesReceived.locales).toEqual([
      {
        displayName: 'Japanese',
        localeId: 'ja',
        nativeName: '日本語'
      },
      {
        displayName: 'Azerbaijani',
        localeId: 'az',
        nativeName: 'azərbaycan dili'
      }
    ])
  })

  it('can track fetching projects', () => {
    const initial = versionReducer(undefState, dummyAction)
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
      meta: { timestamp },
      payload: [
        {
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
        }
      ]
    }
    const initial = versionReducer(undefState, dummyAction)
    const projectsRequested = versionReducer(initial, requestAction)
    const projectsReceived = versionReducer(
      projectsRequested,
      // @ts-ignore
      projectSuccessAction
    )
    expect(projectsRequested.fetchingProject).toEqual(true)
    expect(projectsReceived.fetchingProject).toEqual(false)
    expect(projectsReceived.TMMerge.projectsWithVersions).toEqual([
      {
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
      }
    ])
  })

  it('does not use stale results', () => {
    const stalePayload = [
      {
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
      }
    ]
    const freshPayload = [
      {
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
      }
    ]
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
    const newestResults = versionReducer(
      undefState,
      // @ts-ignore
      secondProjectSuccessAction
    )
    const withStaleAction = versionReducer(
      newestResults,
      // @ts-ignore
      firstProjectSuccessAction
    )

    expect(withStaleAction.projectResultsTimestamp).toEqual(highTea)
    expect(withStaleAction.TMMerge.projectsWithVersions[0]).toEqual(
      newestResults.TMMerge.projectsWithVersions[0]
    )
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
    const initial = versionReducer(undefState, dummyAction)
    const mergeRequested = versionReducer(initial, requestAction)
    // @ts-ignore
    const mergeReceived = versionReducer(mergeRequested, mergeSuccessAction)
    expect(mergeReceived).toEqual(
      /** @type {ProjectVersionState} */ {
        MTMerge: {
          processStatus: undefined,
          queryStatus: undefined,
          showMTMerge: false,
          triggered: false
        },
        TMMerge: {
          processStatus: {
            messages: [],
            percentageComplete: 100,
            statusCode: 'Running',
            url: 'http://localhost:8080/rest/process/key/TMMergeForVerKey-1-ja'
          },
          projectsWithVersions: [],
          queryStatus: undefined,
          show: false,
          triggered: false
        },
        fetchingLocale: false,
        fetchingProject: false,
        locales: [],
        notification: undefined,
        projectResultsTimestamp: initial.projectResultsTimestamp
      }
    )
  })
  it('can track TM merge progress', () => {
    const initial = versionReducer(undefState, dummyAction)
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
    const initial = versionReducer(undefState, dummyAction)
    const cancelRequested = versionReducer(initial, requestAction)
    const cancelReceived = versionReducer(cancelRequested, cancelSuccessAction)
    expect(cancelReceived).toEqual({
      MTMerge: {
        processStatus: undefined,
        queryStatus: undefined,
        showMTMerge: false,
        triggered: false
      },
      TMMerge: {
        processStatus: undefined,
        projectsWithVersions: [],
        queryStatus: undefined,
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
    const initial = versionReducer(undefState, dummyAction)
    const queryRequestAction = {
      type: QUERY_TM_MERGE_PROGRESS_REQUEST
    }
    const queryFailureAction = {
      type: QUERY_TM_MERGE_PROGRESS_FAILURE
    }
    const queryRequested = versionReducer(initial, queryRequestAction)
    const failureRecieved = versionReducer(queryRequested, queryFailureAction)
    expect(failureRecieved).toEqual({
      MTMerge: {
        processStatus: undefined,
        queryStatus: undefined,
        showMTMerge: false,
        triggered: false
      },
      TMMerge: {
        processStatus: undefined,
        projectsWithVersions: [],
        queryStatus: undefined,
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
    const initial = versionReducer(undefState, dummyAction)
    const queryRequestAction = {
      type: TM_MERGE_PROCESS_FINISHED
    }
    const statusRequested = versionReducer(initial, queryRequestAction)
    expect(statusRequested.TMMerge.processStatus).toEqual(undefined)
  })
})
