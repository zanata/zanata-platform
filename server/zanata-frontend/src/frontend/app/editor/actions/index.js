import { createAction } from 'redux-actions'
import {
  ROUTING_PARAMS_CHANGED,
  SET_SIDEBAR_VISIBILITY,
  TOGGLE_DROPDOWN,
  OPEN_DROPDOWN,
  CLOSE_DROPDOWN
} from './action-types'

export const routingParamsChanged = createAction(ROUTING_PARAMS_CHANGED)

/**
 * Every dropdown should have a reference-unique key.
 */
export const toggleDropdown = createAction(TOGGLE_DROPDOWN)
export const openDropdown = createAction(OPEN_DROPDOWN)
export const closeDropdown = createAction(CLOSE_DROPDOWN)
export const setSidebarVisibility = createAction(SET_SIDEBAR_VISIBILITY)
