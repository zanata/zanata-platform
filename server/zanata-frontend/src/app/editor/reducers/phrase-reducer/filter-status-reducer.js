import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from '../../actions/phrases-action-types'

/** @type {import('../state').PhraseFilterStatus} */
export const defaultState = {
  all: true,
  approved: false,
  rejected: false,
  translated: false,
  needswork: false,
  untranslated: false,
  mt: false
}

export default handleActions({
  [RESET_STATUS_FILTERS]: () => defaultState,
  [UPDATE_STATUS_FILTER]: (state, { payload }) => {
    const newState = update(state, {
      // whenever all is true, the default state will be returned instead
      all: {$set: false},
      // @ts-ignore
      [payload]: {$set: !state[payload]}
    })
    // treat all-selected as no filter
    return allStatusesSame(newState) ? defaultState : newState
  }
}, defaultState)

/**
 * Check if statuses are either all true or all false
 * @param filterStatus {import('../state').PhraseFilterStatus}
 */
function allStatusesSame ({
  approved, rejected, translated, needswork, untranslated, mt}) {
  return approved === rejected &&
    rejected === translated &&
    translated === needswork &&
    needswork === untranslated &&
    untranslated === mt
}
