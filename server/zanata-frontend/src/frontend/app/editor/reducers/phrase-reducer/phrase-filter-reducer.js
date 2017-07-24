import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import { composeReducers, subReducer } from 'redux-sac'
import filterStatusReducer, { defaultState as statusDefaultState }
  from './filter-status-reducer'
import {
  TOGGLE_ADVANCED_PHRASE_FILTERS,
  UPDATE_PHRASE_FILTER
} from '../../actions/phrases-action-types'

export const defaultState = {
  showAdvanced: false,
  advanced: {
    text: '',
    resourceId: '',
    lastModifiedBy: '',
    lastModifiedBefore: '',
    lastModifiedAfter: '',
    sourceComment: '',
    translationComment: '',
    msgctxt: ''
  },
  status: statusDefaultState
}

export const phraseFilterReducer = handleActions({
  [TOGGLE_ADVANCED_PHRASE_FILTERS]: (state) =>
    update(state, {showAdvanced: {$set: !state.showAdvanced}}),

  [UPDATE_PHRASE_FILTER]: (state, { payload }) =>
    update(state, { advanced: {$merge: payload} })
}, defaultState)

export default composeReducers(
  phraseFilterReducer,
  subReducer('status', filterStatusReducer)
)
