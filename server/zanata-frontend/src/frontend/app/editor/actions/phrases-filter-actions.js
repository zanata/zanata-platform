import { createAction } from 'redux-actions'
import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from './phrases-action-types'
import { clampPage } from './controls-header-actions'

export const resetStatusFilter = createAction(RESET_STATUS_FILTERS)

export function updateStatusFilter (status) {
  return (dispatch) => {
    dispatch(createAction(UPDATE_STATUS_FILTER)(status))
    // needed in case there is a different page count after filtering
    dispatch(clampPage())
  }
}
