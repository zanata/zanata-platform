import {handleActions} from 'redux-actions'
import {cloneDeep} from 'lodash'
import {
  TOGGLE_TM_MERGE_MODAL
} from '../actions/version-actions'

export default handleActions({
  [TOGGLE_TM_MERGE_MODAL]: (state, action) => {
    let newState = cloneDeep(state)
    newState.TMMerge.show = !state.TMMerge.show
    return {
      ...newState
    }
  }},
// default state
  {
    TMMerge: {
      show: false
    }
  })
