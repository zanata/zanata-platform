import {handleActions} from 'redux-actions'
import update from 'immutability-helper'
import {
  TOGGLE_MT_MERGE_MODAL,
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_REQUEST,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_REQUEST,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE,
  VERSION_MT_MERGE_REQUEST,
  VERSION_MT_MERGE_SUCCESS,
  VERSION_MT_MERGE_FAILURE,
  VERSION_TM_MERGE_REQUEST,
  VERSION_TM_MERGE_SUCCESS,
  VERSION_TM_MERGE_FAILURE,
  QUERY_MT_MERGE_PROGRESS_SUCCESS,
  QUERY_MT_MERGE_PROGRESS_FAILURE,
  QUERY_TM_MERGE_PROGRESS_SUCCESS,
  QUERY_TM_MERGE_PROGRESS_FAILURE,
  MT_MERGE_PROCESS_FINISHED,
  TM_MERGE_PROCESS_FINISHED,
  MT_MERGE_CANCEL_FAILURE,
  MT_MERGE_CANCEL_SUCCESS
} from '../actions/version-action-types'
import { SEVERITY, statusToSeverity } from '../actions/common-actions'
import { filter, isEmpty } from 'lodash'

/** @typedef {import('./state').ProjectVersionState} ProjectVersionState */

/** @type {ProjectVersionState} */
export const defaultState = {
  // See mapReduxStateToProps in MTMergeContainer.ts
  MTMerge: {
    showMTMerge: false,
    triggered: false,
    processStatus: undefined,
    queryStatus: undefined,
  },
  TMMerge: {
    show: false,
    triggered: false,
    processStatus: undefined,
    queryStatus: undefined,
    projectsWithVersions: []
  },
  // this works unless the code is sent back in time to the 1960s or earlier.
  projectResultsTimestamp: new Date(0),
  locales: [],
  fetchingProject: false,
  fetchingLocale: false,
  notification: undefined
}

// TODO this seems redundant, but is needed for the test to compile.
// Should be fixed by Redux 4: https://github.com/reactjs/redux/pull/2773
/** @type {import('redux').Reducer<ProjectVersionState>} */
const version = handleActions({
  [TOGGLE_MT_MERGE_MODAL]: (state, _action) => {
    return update(state, {
      MTMerge: { showMTMerge: { $set: !state.MTMerge.showMTMerge } }
    })
  },
  [TOGGLE_TM_MERGE_MODAL]: (state, _action) => {
    return update(state, {
      TMMerge: { show: { $set: !state.TMMerge.show } }
    })
  },
  [VERSION_LOCALES_REQUEST]: (state, action) => {
    return action.error ? update(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        severity: 'error',
        message: 'We were unable load locale information. ' +
          'Please refresh this page and try again.',
        duration: null
      }}
    }) : update(state, {
      fetchingLocale: { $set: true },
      notification: { $set: undefined }
    })
  },
  [VERSION_LOCALES_SUCCESS]: (state, action) => {
    return update(state, {
      locales: { $set: action.payload },
      fetchingLocale: { $set: false },
      notification: { $set: undefined }
    })
  },
  [VERSION_LOCALES_FAILURE]: (state, _action) => {
    return update(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        severity: 'error',
        message: 'We were unable load locale information. ' +
          'Please refresh this page and try again.',
        duration: null
      }}
    })
  },
  [PROJECT_PAGE_REQUEST]: (state, action) => {
    return action.error ? update(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        severity: 'error',
        message: 'We were unable load project information. ' +
          'Please refresh this page and try again.',
        duration: null
      }}
    }) : update(state, {
      fetchingProject: { $set: true },
      notification: { $set: undefined }
    })
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    // @ts-ignore
    if (action.meta.timestamp > state.projectResultsTimestamp) {
      // filter out project with empty versions
      const filteredProjects = filter(action.payload, (project) => {
        return !isEmpty(project.versions)
      })
      return update(state, {
        TMMerge: { projectsWithVersions: { $set: filteredProjects } },
        fetchingProject: { $set: false },
        notification: { $set: undefined },
        // @ts-ignore
        projectResultsTimestamp: {$set: action.meta.timestamp}
      })
    } else {
      return state
    }
  },
  [PROJECT_PAGE_FAILURE]: (state, _action) => {
    return update(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        severity: 'error',
        message: 'We were unable load project information. ' +
          'Please refresh this page and try again.',
        duration: null
      }}
    })
  },
  [VERSION_TM_MERGE_REQUEST]: (state, _action) => {
    return update(state, {
      TMMerge: { triggered: { $set: true } },
      notification: { $set: undefined }
    })
  },
  [VERSION_TM_MERGE_SUCCESS]: (state, action) => {
    return update(state, {
      TMMerge:
        { processStatus: { $set: action.payload }, triggered: { $set: false } }
    })
  },
  [VERSION_TM_MERGE_FAILURE]: (state, action) => {
    const defaultMsg = 'We were unable perform the operation. Please try again.'
    // @ts-ignore
    const response = action && action.payload ? action.payload.response
      : undefined
    const msg = (response && response.error ? ' (' + response.error + ')' : '')
    return update(state, {
      TMMerge: { triggered: { $set: false } },
      notification: { $set: {
        severity: 'error',
        message: defaultMsg,
        description: msg,
        duration: null
      } }
    })
  },
  [VERSION_MT_MERGE_REQUEST]: (state, _action) => {
    return update(state, {
      MTMerge: { triggered: { $set: true } },
      notification: { $set: undefined }
    })
  },
  [VERSION_MT_MERGE_SUCCESS]: (state, action) => {
    return update(state, {
      MTMerge:
        { processStatus: { $set: action.payload }, triggered: { $set: false } }
    })
  },
  [VERSION_MT_MERGE_FAILURE]: (state, action) => {
    const defaultMsg = 'We were unable perform the operation. Please try again.'
    // @ts-ignore
    const response = action && action.payload ? action.payload.response
      : undefined
    const msg = (response && response.error ? ' (' + response.error + ')' : '')
    return update(state, {
      MTMerge: { triggered: { $set: false } },
      notification: {
        $set: {
          severity: 'error',
          message: defaultMsg,
          description: msg,
          duration: null
        }
      }
    })
  },
  [QUERY_MT_MERGE_PROGRESS_SUCCESS]: (state, action) => {
    return update(state, {
      // Use merge to ensure cancelUrl is not lost
      MTMerge: { processStatus: { $set: action.payload } }
    })
  },
  [QUERY_MT_MERGE_PROGRESS_FAILURE]: (state, action) => {
    // what do we do with failed status query?
    return update(state, {
      MTMerge: { queryStatus: { $set: action.error } }
    })
  },
  [QUERY_TM_MERGE_PROGRESS_SUCCESS]: (state, action) => {
    // @ts-ignore
    return update(state, {
      // Using merge to ensure cancelUrl is not lost
      TMMerge: { processStatus: { $merge: action.payload } }
    })
  },
  [QUERY_TM_MERGE_PROGRESS_FAILURE]: (state, action) => {
    // what do we do with failed status query?
    return update(state, {
      TMMerge: { queryStatus: { $set: action.error } }
    })
  },
  [MT_MERGE_PROCESS_FINISHED]: (state, _action) => {
    return update(state, {
      // MTMerge: { processStatus: { $set: undefined } },
      notification: {
        $set: {
          severity: `${state.MTMerge.processStatus
            ? statusToSeverity(state.MTMerge.processStatus.statusCode)
            : SEVERITY.INFO}`,
          message: `MT Merge finished ${state.MTMerge.processStatus
            ? 'with status: ' + state.MTMerge.processStatus.statusCode : ''}`,
          duration: SEVERITY.ERROR ? null : 3.5
        }
      }
    })
  },
  [MT_MERGE_CANCEL_SUCCESS]: (state, _action) => {
    return update(state, {
      MTMerge: { processStatus: { $set: undefined } },
      notification: {
        $set: {
          severity: SEVERITY.SUCCESS,
          message: 'MT Merge cancelled successfully',
        }
      }
    })
  },
  [MT_MERGE_CANCEL_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: 'Cancel MT Merge request failed',
          description: action.error,
          duration: null
        }
      }
    })
  },
  [TM_MERGE_PROCESS_FINISHED]: (state, _action) => {
    return update(state, {
      TMMerge: { processStatus: { $set: undefined } }
    })
  }},
  defaultState)

export default version
