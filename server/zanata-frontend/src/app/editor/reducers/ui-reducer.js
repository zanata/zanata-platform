/* eslint-disable spaced-comment */
/* @flow */ // TODO convert to TS
import { handleActions } from 'redux-actions'
import { createSelector } from 'reselect'
import {
  SET_SIDEBAR_VISIBILITY,
  TOGGLE_SHOW_SETTINGS,
  HIDE_SETTINGS
} from '../actions/action-types'
import {
  CHANGE_UI_LOCALE,
  TOGGLE_GLOSSARY,
  TOGGLE_INFO_PANEL,
  TOGGLE_HEADER,
  TOGGLE_KEY_SHORTCUTS,
  UI_LOCALES_FETCHED,
  APP_LOCALE_FETCHED
} from '../actions/header-action-types'
import {
  SUGGESTION_PANEL_HEIGHT_CHANGE
} from '../actions/suggestions-action-types'
import { prepareLocales } from '../utils/Util'
import update from 'immutability-helper'

// TODO extract this to a common config
export const DEFAULT_LOCALE = {
  localeId: 'en-US',
  name: 'English',
  isRTL: false
}

export const GLOSSARY_TAB = 'GLOSSARY_TAB'
export const identity = (key/*: any*/) => {
  // TODO pahuang implement gettextCatalog.getString
  // console.log('gettextCatalog.getString')
  return key
}

/*::
type State = {
  +panels: {
    +navHeader: {
      +visible: bool
    },
    +sidebar: {
      +visible: bool,
      +selectedTab: 'GLOSSARY_TAB'
    },
    +suggestions: {
      +heightPercent: number
    },
    +keyShortcuts: {
      +visible: bool
    }
  },
  +showSettings: bool
}
*/

const defaultState /*: State*/ = {
  panels: {
    navHeader: {
      visible: true
    },
    sidebar: {
      visible: true,
      selectedTab: GLOSSARY_TAB
    },
    suggestions: {
      heightPercent: 0.3
    },
    keyShortcuts: {
      visible: false
    }
  },
  uiLocales: {},
  selectedUiLocale: DEFAULT_LOCALE.localeId,
  showSettings: false,
  gettextCatalog: {
    getString: identity
  }
}

/* selectors */
export const getNavHeaderVisible =
  (state/*: State*/) => state.panels.navHeader.visible
// always show sidebar when settings is on
export const getSidebarVisible = (state/*: State*/) =>
  state.panels.sidebar.visible || state.showSettings
export const getSidebarTab = (state/*: State*/) =>
  state.panels.sidebar.selectedTab
export const getShowSettings = (state/*: State*/) => state.showSettings
export const getGlossaryVisible = createSelector(getSidebarVisible,
  getShowSettings, getSidebarTab,
    (sidebar, settings, tab) => sidebar && !settings && tab === GLOSSARY_TAB)
// info panel is always-on in the non-settings sidebar
export const getInfoPanelVisible = createSelector(getSidebarVisible,
  getShowSettings, (sidebar, settings) => sidebar && !settings)
export const getKeyShortcutsVisible =
  (state/*: State*/) => state.panels.keyShortcuts.visible

/* instruct immutability-helper to toggle a boolean value */
const $toggle = {$apply: bool => !bool}

export default handleActions({
  [SUGGESTION_PANEL_HEIGHT_CHANGE]: (state, { payload }) => update(state,
    { panels: { suggestions: { heightPercent: {$set: payload} } } }),

  // selectedTab and showSettings will always be the same after this toggle,
  // either they already had the values below, or we needed to set them.
  [TOGGLE_GLOSSARY]: state => update(state, {
    panels: {
      sidebar: {
        visible: {$set: !getGlossaryVisible(state)},
        selectedTab: {$set: GLOSSARY_TAB}
      }
    },
    showSettings: {$set: false}
  }),

  [TOGGLE_INFO_PANEL]: state => update(state, {
    panels: {
      sidebar: {
        visible: {$set: !getInfoPanelVisible(state)}
      }
    },
    // was either already hidden, or needs to be hidden to see info panel
    showSettings: {$set: false}
  }),

  [TOGGLE_HEADER]: state => update(state, {
    panels: {
      navHeader: {
        visible: $toggle
      }
    }
  }),

  [UI_LOCALES_FETCHED]: (state, { payload }) => update(state, {
    uiLocales: {$set: prepareLocales(payload)}
  }),

  [APP_LOCALE_FETCHED]: (state, { payload }) => update(state, {
    appLocaleData: {
      $set: payload
    }
  }),

  [TOGGLE_KEY_SHORTCUTS]: state => update(state, {
    panels: {
      keyShortcuts: {
        visible: $toggle
      }
    }
  }),

  [CHANGE_UI_LOCALE]: (state, { payload }) => update(state, {
    selectedUiLocale: {
      $set: payload
    }
  }),

  [SET_SIDEBAR_VISIBILITY]: (state, { payload }) => update(state, {
    panels: {
      sidebar: {
        visible: {$set: payload}
      }
    }
  }),

  [TOGGLE_SHOW_SETTINGS]: state => update(state, { showSettings: $toggle }),

  [HIDE_SETTINGS]: (state) => update(state, { showSettings: {$set: false} })
}, defaultState)
