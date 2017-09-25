import { SET_SIDEBAR_VISIBILITY } from '../actions/action-types'
import {
  CHANGE_UI_LOCALE,
  TOGGLE_GLOSSARY,
  TOGGLE_HEADER,
  TOGGLE_KEY_SHORTCUTS,
  UI_LOCALES_FETCHED
} from '../actions/header-action-types'
import {
  SUGGESTION_PANEL_HEIGHT_CHANGE,
  TOGGLE_SUGGESTIONS
} from '../actions/suggestions-action-types'
import {prepareLocales} from '../utils/Util'
import updateObject from 'immutability-helper'

// TODO extract this to a common config
export const DEFAULT_LOCALE = {
  'localeId': 'en-US',
  'name': 'English'
}

export const GLOSSARY_TAB = Symbol('GLOSSARY_TAB')
export const identity = (key) => {
  // TODO pahuang implement gettextCatalog.getString
  // console.log('gettextCatalog.getString')
  return key
}

const defaultState = {
  panels: {
    navHeader: {
      visible: true
    },
    sidebar: {
      visible: true,
      selectedTab: GLOSSARY_TAB
    },
    suggestions: {
      visible: true,
      heightPercent: 0.3
    },
    keyShortcuts: {
      visible: false
    }
  },
  uiLocales: {},
  selectedUiLocale: DEFAULT_LOCALE.localeId,
  gettextCatalog: {
    getString: identity
  }
}

const ui = (state = defaultState, action) => {
  switch (action.type) {
    case SUGGESTION_PANEL_HEIGHT_CHANGE:
      return update({
        panels: {
          suggestions: {
            heightPercent: {$set: action.payload}
          }
        }
      })

    case TOGGLE_GLOSSARY:
      const glossaryWasOpen = state.panels.sidebar.visible &&
        state.panels.sidebar.selectedTab === GLOSSARY_TAB
      return update({
        panels: {
          sidebar: {
            visible: {$set: !glossaryWasOpen},
            selectedTab: {$set: GLOSSARY_TAB}
          }
        }
      })

    case TOGGLE_HEADER:
      return update({
        panels: {
          navHeader: {
            visible: {$set: !state.panels.navHeader.visible}
          }
        }
      })

    case TOGGLE_SUGGESTIONS:
      return update({
        panels: {
          suggestions: {
            visible: {$set: !state.panels.suggestions.visible}
          }
        }
      })

    case UI_LOCALES_FETCHED:
      const locales = prepareLocales(action.payload)
      return update({
        uiLocales: {
          $set: locales
        }
      })

    case TOGGLE_KEY_SHORTCUTS:
      return update({
        panels: {
          keyShortcuts: {
            visible: {$set: !state.panels.keyShortcuts.visible}
          }
        }
      })

    case CHANGE_UI_LOCALE:
      // TODO pahuang implement change ui locale
      /*
       appCtrl.myInfo.locale = locale
       var uiLocaleId = appCtrl.myInfo.locale.localeId
       if (!StringUtil.startsWith(uiLocaleId,RESET_STATUS_FILTERS
       LocaleService.DEFAULT_LOCALE.localeId, true)) {
       gettextCatalog.loadRemote(UrlService.uiTranslationURL(uiLocaleId))
       .then(
       function () {
       gettextCatalog.setCurrentLanguage(uiLocaleId)
       },
       function (error) {
       MessageHandler.displayInfo('Error loading UI locale. ' +
       'Default to \'' + LocaleService.DEFAULT_LOCALE.name +
       '\': ' + error)
       gettextCatalog.setCurrentLanguage(
       LocaleService.DEFAULT_LOCALE)
       appCtrl.myInfo.locale = LocaleService.DEFAULT_LOCALE
       })
       } else {
       // wrapped in apply because this MUST be run at the appropriate part of
       // the angular cycle, or it does not remove the old strings from the UI
       // (you end up with multiple strings displaying).
       $scope.$apply(function () {
       gettextCatalog.setCurrentLanguage(
       LocaleService.DEFAULT_LOCALE.localeId)
       })
       }
       */
      return update({
        selectedUiLocale: {
          $set: action.payload
        }
      })

    case SET_SIDEBAR_VISIBILITY:
      return update({
        panels: {
          sidebar: {
            visible: {$set: action.payload}
          }
        }
      })

    default:
      return state
  }

  /**
   * Apply the given commands to state.
   *
   * Just a shortcut to avoid having to pass state to update over and over.
   */
  function update (commands) {
    // FIXME update to version that does not lose reference equality when
    //       setting an identical object
    //       see: https://github.com/facebook/react/pull/4968
    return updateObject(state, commands)
  }
}

export default ui
