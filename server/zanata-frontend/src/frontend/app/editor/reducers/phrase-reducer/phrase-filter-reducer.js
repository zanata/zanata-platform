// import { handleActions } from 'redux-actions'
// import { update } from 'immutability-helper'
import { composeReducers, subReducer } from 'redux-sac'
import filterStatusReducer from './filter-status-reducer'

const defaultState = {}

// TODO advanced search data can be handled here
const phraseFilterReducer = (state) => (state || defaultState)

export default composeReducers(
  phraseFilterReducer,
  subReducer('status', filterStatusReducer)
)
