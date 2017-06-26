import {handleActions} from 'redux-actions'
import {cloneDeep} from 'lodash'
import {SEVERITY} from '../actions/common-actions'
import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_SUCCESS,
  VERSION_LOCALES_FAILURE,
  PROJECT_PAGE_SUCCESS,
  PROJECT_PAGE_FAILURE
} from '../actions/version-action-types'

const version = handleActions({
  [TOGGLE_TM_MERGE_MODAL]: (state, action) => {
    let newState = cloneDeep(state)
    newState.TMMerge.show = !state.TMMerge.show
    return {
      ...newState
    }
  },
  [VERSION_LOCALES_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.locales = action.payload
    return {
      ...newState,
      loading: false,
      notification: undefined
    }
  },
  [VERSION_LOCALES_FAILURE]: (state, action) => {
    return {
      ...state,
      loading: false,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load user information. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.TMMerge.projectVersions = action.payload
    return {
      ...newState,
      loading: false,
      notification: undefined
    }
  },
  [PROJECT_PAGE_FAILURE]: (state, action) => {
    return {
      ...state,
      loading: false,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load project information. ' +
        'Please refresh this page and try again.'
      }
    }
  }},
// default state
  {
    TMMerge: {
      show: false,
      projectVersions: []
    },
    locales: []
  })

export default version
