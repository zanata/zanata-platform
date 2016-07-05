import updateObject from 'react-addons-update'
import {
  OPEN_DROPDOWN, CLOSE_DROPDOWN, TOGGLE_DROPDOWN
} from '../actions'

const defaultState = {
  openDropdownKey: undefined,
  docsKey: Symbol('Documents Dropdown'),
  localeKey: Symbol('Locales Dropdown'),
  uiLocaleKey: Symbol('UI Locales Dropdown')
}

/**
 * State is just the key of the currently open dropdown, or undefined if
 * no dropdown is open.
 */
const dropdownReducer = (state = defaultState, action) => {
  switch (action.type) {
    case OPEN_DROPDOWN:
      return update({
        openDropdownKey: {$set: action.key}
      })

    case CLOSE_DROPDOWN:
      return update({
        openDropdownKey: {$set: undefined}
      })

    case TOGGLE_DROPDOWN:
      const isOpen = action.key === state.openDropdownKey
      return update({
        openDropdownKey: {$set: isOpen ? undefined : action.key}
      })

    default:
      return state
  }

  function update (commands) {
    return updateObject(state, commands)
  }
}

export default dropdownReducer
