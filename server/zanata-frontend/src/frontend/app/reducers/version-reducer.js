import {handleActions} from 'redux-actions'
import {cloneDeep} from 'lodash'
import {SEVERITY} from '../actions/common-actions'
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
    const newState = cloneDeep(state)
    newState.TMMerge.show = !state.TMMerge.show
    return {
      ...newState
    }
  },
  [VERSION_LOCALES_REQUEST]: (state, action) => {
    const newState = cloneDeep(state)
    if (action.error) {
      return {
        ...newState,
        fetchingLocale: false,
        notification: {
          severity: SEVERITY.ERROR,
          message: 'We were unable load locale information. ' +
          'Please refresh this page and try again.'
        }
      }
    }
    return {
      ...newState,
      fetchingLocale: true,
      notification: undefined
    }
  },
  [VERSION_LOCALES_SUCCESS]: (state, action) => {
    const newState = cloneDeep(state)
    newState.locales = action.payload
    return {
      ...newState,
      fetchingLocale: false,
      notification: undefined
    }
  },
  [VERSION_LOCALES_FAILURE]: (state, action) => {
    const newState = cloneDeep(state)
    return {
      ...newState,
      fetchingLocale: false,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load locale information. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [PROJECT_PAGE_REQUEST]: (state, action) => {
    const newState = cloneDeep(state)
    if (action.error) {
      return {
        ...newState,
        fetchingProject: false,
        notification: {
          severity: SEVERITY.ERROR,
          message:
          'We were unable load project information. ' +
          'Please refresh this page and try again.'
        }
      }
    }
    return {
      ...newState,
      fetchingProject: true,
      notification: undefined
    }
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.projectVersions = action.payload
    return {
      ...newState,
      fetchingProject: false,
      notification: undefined
    }
  },
  [PROJECT_PAGE_FAILURE]: (state, action) => {
    const newState = cloneDeep(state)
    return {
      ...newState,
      fetchingProject: false,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [VERSION_TM_MERGE_REQUEST]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.triggered = true
    return {
      ...newState,
      notification: undefined
    }
  },
  [VERSION_TM_MERGE_SUCCESS]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.processStatus = action.payload
    newState.TMMerge.triggered = false
    return {
      ...newState,
      notification: undefined
    }
  },
  [VERSION_TM_MERGE_FAILURE]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.triggered = false
    return {
      ...newState,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable perform the operation. Please try again.'
      }
    }
  },
  [QUERY_TM_MERGE_PROGRESS_SUCCESS]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.processStatus = action.payload
    return newState
  },
  [QUERY_TM_MERGE_PROGRESS_FAILURE]: (state, action) => {
    // what do we do with failed status query?
    console.error(
      `failed getting process status for version TM merge ${action.error}`)
    return state
  },
  [TM_MERGE_PROCESS_FINISHED]: (state, action) => {
    const newState = cloneDeep(state)
    newState.TMMerge.processStatus = undefined
    return newState
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
