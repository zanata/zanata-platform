import { handleActions } from 'redux-actions'
import { TRANS_HISTORY_SUCCESS } from '../actions/activity-action-types'
import update from 'immutability-helper'

/** @type {import('./state').ActivityState} */
const defaultState = {
  transHistory: {
    historyItems: undefined,
    latest: undefined,
    reviewComments: undefined
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
