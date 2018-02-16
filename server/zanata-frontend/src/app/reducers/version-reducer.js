// @ts-nocheck
import {handleActions} from 'redux-actions'
import update from 'immutability-helper'
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
  QUERY_TM_MERGE_PROGRESS_SUCCESS,
  QUERY_TM_MERGE_PROGRESS_FAILURE,
  TM_MERGE_PROCESS_FINISHED
} from '../actions/version-action-types'

const defaultState = {
  TMMerge: {
    show: false,
    triggered: false,
    processStatus: undefined,
    queryStatus: undefined,
    projectVersions: []
  },
  // this works unless the code is sent back in time to the 1960s or earlier.
  projectResultsTimestamp: new Date(0),
  locales: [],
  fetchingProject: false,
  fetchingLocale: false,
  notification: undefined
}

const version = handleActions({
  [TOGGLE_TM_MERGE_MODAL]: (state, action) => {
    return update(state, {
      TMMerge: { show: { $set: !state.TMMerge.show } }
    })
  },
  [VERSION_LOCALES_REQUEST]: (state, action) => {
    return action.error ? update(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        message: 'We were unable load locale information. ' +
        'Please refresh this page and try again.'
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
  [VERSION_LOCALES_FAILURE]: (state, action) => {
    return update(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        message: 'We were unable load locale information. ' +
        'Please refresh this page and try again.'
      }}
    })
  },
  [PROJECT_PAGE_REQUEST]: (state, action) => {
    return action.error ? update(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        message: 'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }}
    }) : update(state, {
      fetchingProject: { $set: true },
      notification: { $set: undefined }
    })
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    if (action.meta.timestamp > state.projectResultsTimestamp) {
      return update(state, {
        TMMerge: { projectVersions: { $set: action.payload } },
        fetchingProject: { $set: false },
        notification: { $set: undefined },
        projectResultsTimestamp: {$set: action.meta.timestamp}
      })
    } else {
      return state
    }
  },
  [PROJECT_PAGE_FAILURE]: (state, action) => {
    return update(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        message: 'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }}
    })
  },
  [VERSION_TM_MERGE_REQUEST]: (state, action) => {
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
    const response = action && action.payload ? action.payload.response
      : undefined
    const msg = defaultMsg +
      (response && response.error ? ' (' + response.error + ')' : '')
    return update(state, {
      TMMerge: { triggered: { $set: false } },
      notification: { $set: {
        message: msg
      } }
    })
  },
  [QUERY_TM_MERGE_PROGRESS_SUCCESS]: (state, action) => {
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
  [TM_MERGE_PROCESS_FINISHED]: (state, action) => {
    return update(state, {
      TMMerge: { processStatus: { $set: undefined } }
    })
  }},
  defaultState)

export default version
