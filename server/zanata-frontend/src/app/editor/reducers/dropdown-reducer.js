import update from 'immutability-helper'
import { handleActions } from 'redux-actions'
import {
  OPEN_DROPDOWN, CLOSE_DROPDOWN, TOGGLE_DROPDOWN
} from '../actions/action-types'

const defaultState = {
  openDropdownKey: undefined,
  docsKey: 'Documents Dropdown',
  localeKey: 'Locales Dropdown',
  uiLocaleKey: 'UI Locales Dropdown'
}

const dropdownReducer = handleActions({
  [OPEN_DROPDOWN]: (state, { payload }) =>
    update(state, { openDropdownKey: {$set: payload} }),

  [CLOSE_DROPDOWN]: state =>
    update(state, { openDropdownKey: {$set: undefined} }),

  [TOGGLE_DROPDOWN]: (state, { payload }) => update(state, { openDropdownKey:
    {$set: payload === state.openDropdownKey ? undefined : payload}
  })
}, defaultState)

export default dropdownReducer
