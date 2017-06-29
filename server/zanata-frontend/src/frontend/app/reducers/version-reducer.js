import {handleActions} from 'redux-actions'
import updateObject from 'immutability-helper'
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

const version = handleActions({
  [TOGGLE_TM_MERGE_MODAL]: (state, action) => {
    return updateObject(state, {
      TMMerge: { show: { $set: !state.TMMerge.show } }
    })
  },
  [VERSION_LOCALES_REQUEST]: (state, action) => {
    return action.error ? updateObject(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        message: 'We were unable load locale information. ' +
        'Please refresh this page and try again.'
      }}
    }) : updateObject(state, {
      fetchingLocale: { $set: true },
      notification: { $set: undefined }
    })
  },
  [VERSION_LOCALES_SUCCESS]: (state, action) => {
    return updateObject(state, {
      locales: { $set: action.payload },
      fetchingLocale: { $set: false },
      notification: { $set: undefined }
    })
  },
  [VERSION_LOCALES_FAILURE]: (state, action) => {
    return updateObject(state, {
      fetchingLocale: { $set: false },
      notification: { $set: {
        message: 'We were unable load locale information. ' +
        'Please refresh this page and try again.'
      }}
    })
  },
  [PROJECT_PAGE_REQUEST]: (state, action) => {
    return action.error ? updateObject(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        message: 'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }}
    }) : updateObject(state, {
      fetchingProject: { $set: true },
      notification: { $set: undefined }
    })
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    return updateObject(state, {
      TMMerge: { projectVersions: { $set: action.payload } },
      fetchingProject: { $set: false },
      notification: { $set: undefined }
    })
  },
  [PROJECT_PAGE_FAILURE]: (state, action) => {
    return updateObject(state, {
      fetchingProject: { $set: false },
      notification: { $set: {
        message: 'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }}
    })
  },
  [VERSION_TM_MERGE_REQUEST]: (state, action) => {
    return updateObject(state, {
      TMMerge: { triggered: { $set: true } },
      notification: { $set: undefined }
    })
  },
  [VERSION_TM_MERGE_SUCCESS]: (state, action) => {
    return updateObject(state, {
      TMMerge:
        { processStatus: { $set: action.payload }, triggered: { $set: false } }
    })
  },
  [VERSION_TM_MERGE_FAILURE]: (state, action) => {
    return updateObject(state, {
      TMMerge: { triggered: { $set: false } },
      notification: { $set: {
        message:
          'We were unable perform the operation. Please try again.'
      } }
    })
  },
  [QUERY_TM_MERGE_PROGRESS_SUCCESS]: (state, action) => {
    return updateObject(state, {
      TMMerge: { processStatus: { $set: action.payload } }
    })
  },
  [QUERY_TM_MERGE_PROGRESS_FAILURE]: (state, action) => {
    // what do we do with failed status query?
    console.error(
      `failed getting process status for version TM merge ${action.error}`)
    return state
  },
  [TM_MERGE_PROCESS_FINISHED]: (state, action) => {
    return updateObject(state, {
      TMMerge: { processStatus: { $set: undefined } }
    })
  }},
// default state
  {
    TMMerge: {
      show: false,
      triggered: false,
      processStatus: undefined,
      projectVersions: []
    },
    locales: [],
    fetchingProject: false,
    fetchingLocale: false,
    notification: undefined
  })

export default version
