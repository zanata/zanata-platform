import { handleActions } from 'redux-actions'
import { TRANS_HISTORY_SUCCESS } from '../actions/activity-action-types'
import update from 'immutability-helper'

const defaultState = {
  transHistory: {
    historyItems: {},
    latest: {},
    reviewComments: {}
  }
}

const activity = handleActions({
  [TRANS_HISTORY_SUCCESS]: (state, action) => {
    return update(state, {
      transHistory: { $set: action.payload }
    })
  }
}, defaultState)

export default activity
