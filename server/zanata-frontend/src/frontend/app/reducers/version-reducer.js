import {handleActions} from 'redux-actions'
import {cloneDeep} from 'lodash'
import {
  TOGGLE_TM_MERGE_MODAL,
  VERSION_LOCALES_SUCCESS,
  PROJECT_PAGE_SUCCESS
} from '../actions/version-action-types'

export default handleActions({
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
      ...newState
    }
  },
  [PROJECT_PAGE_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.TMMerge.projectVersions = action.payload
    return {
      ...newState
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
