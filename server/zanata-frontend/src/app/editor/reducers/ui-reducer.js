/* eslint-disable spaced-comment */
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
  TOGGLE_ACTIVITY,
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
import { DEFAULT_LOCALE } from '../../config'

export const GLOSSARY_TAB = 'GLOSSARY_TAB'
export const ACTIVITY_TAB = 'ACTIVITY_TAB'

/** @param key {string} */
export const identity = (key) => {
  // TODO pahuang implement gettextCatalog.getString
  // console.log('gettextCatalog.getString')
  return key
}

/** @type {import('./state').EditorUIState} */
const defaultState = {
  panels: {
    navHeader: {
      visible: true
    },
    sidebar: {
      visible: true,
      selectedTab: ACTIVITY_TAB
    },
    suggestions: {
      heightPercent: 0.3
    },
    keyShortcuts: {
      visible: false
    }
  },
  appLocaleData: undefined,
  uiLocales: {},
  selectedUiLocale: DEFAULT_LOCALE.localeId,
  showSettings: false,
  gettextCatalog: {
    getString: identity
  }
}

/* selectors */
export const getNavHeaderVisible =
  /** @param state {import('./state').EditorUIState} */
  (state) => state.panels.navHeader.visible
// always show sidebar when settings is on
/** @param state {import('./state').EditorUIState} */
export const getSidebarVisible = (state) =>
  state.panels.sidebar.visible || state.showSettings
/** @param state {import('./state').EditorUIState} */
export const getSidebarTab = (state) =>
  state.panels.sidebar.selectedTab
/** @param state {import('./state').EditorUIState} */
export const getShowSettings = (state) => state.showSettings
export const getGlossaryVisible = createSelector(getSidebarVisible,
  getShowSettings, getSidebarTab,
    (sidebar, settings, tab) => sidebar && !settings && tab === GLOSSARY_TAB)
export const getActivityVisible = createSelector(getSidebarVisible,
  getShowSettings, getSidebarTab,
    (sidebar, settings, tab) => sidebar && !settings && tab === ACTIVITY_TAB)
// info panel is always-on in the non-settings sidebar
export const getInfoPanelVisible = createSelector(getSidebarVisible,
  getShowSettings, (sidebar, settings) => sidebar && !settings)
export const getKeyShortcutsVisible =
  /** @param state {import('./state').EditorUIState} */
  (state) => state.panels.keyShortcuts.visible

/* instruct immutability-helper to toggle a boolean value */
// @ts-ignore any
const $toggle = {$apply: bool => !bool}

export default handleActions({
  [SUGGESTION_PANEL_HEIGHT_CHANGE]: (state, { payload }) => update(state,
    { panels: { suggestions: { heightPercent: {$set: payload} } } }),

  // selectedTab and showSettings will always be the same after this toggle,
  // either they already had the values below, or we needed to set them.
  [TOGGLE_GLOSSARY]: state => update(state, {
    panels: {
      sidebar: {
        visible: {$set: true},
        selectedTab: {$set: (
          getSidebarTab(state) === GLOSSARY_TAB ? ACTIVITY_TAB : GLOSSARY_TAB)
        }
      }
    },
    showSettings: {$set: false}
  }),

  [TOGGLE_ACTIVITY]: state => update(state, {
    panels: {
      sidebar: {
        visible: {$set: true},
        selectedTab: {$set: (
          getSidebarTab(state) === GLOSSARY_TAB ? ACTIVITY_TAB : GLOSSARY_TAB)
        }
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
