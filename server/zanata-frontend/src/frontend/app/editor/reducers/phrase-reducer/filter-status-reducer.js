import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from '../../actions/phrases-action-types'

const defaultState = {
  all: true,
  approved: false,
  rejected: false,
  translated: false,
  needswork: false,
  untranslated: false
}

export default handleActions({
  [RESET_STATUS_FILTERS]: () => defaultState,
  [UPDATE_STATUS_FILTER]: (state, { status }) => {
    const newState = update(state, {
      // whenever all is true, the default state will be returned instead
      all: {$set: false},
      [status]: {$set: !state[status]}
    })
    // treat all-selected as no filter
    return allStatusesSame(newState) ? defaultState : newState
  }
}, defaultState)

/**
 * Check if statuses are either all true or all false
 */
function allStatusesSame (statuses) {
  return statuses.approved === statuses.rejected &&
    statuses.rejected === statuses.translated &&
    statuses.translated === statuses.needswork &&
    statuses.needswork === statuses.untranslated
}
