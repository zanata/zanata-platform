import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from './phrases-action-types'
import { clampPage } from './controls-header-actions'

export function resetStatusFilter () {
  return {type: RESET_STATUS_FILTERS}
}

export function updateStatusFilter (status) {
  return (dispatch) => {
    dispatch({type: UPDATE_STATUS_FILTER, status})
    // needed in case there is a different page count after filtering
    dispatch(clampPage())
  }
}
