import { SET_SIDEBAR_VISIBILITY } from '../actions'
import {
  CHANGE_UI_LOCALE,
  TOGGLE_HEADER,
  TOGGLE_KEY_SHORTCUTS,
  UI_LOCALES_FETCHED
} from '../actions/headerActions'
import {
  RESET_STATUS_FILTERS,
  UPDATE_STATUS_FILTER
} from '../actions/controlsHeaderActions'
import {
  SUGGESTION_PANEL_HEIGHT_CHANGE,
  TOGGLE_SUGGESTIONS
} from '../actions/suggestions'
import {prepareLocales} from '../utils/Util'
import updateObject from 'react-addons-update'

// TODO extract this to a common config
export const DEFAULT_LOCALE = {
  'localeId': 'en-US',
  'name': 'English'
}

const DEFAULT_FILTER_STATE = {
  all: true,
  approved: false,
  rejected: false,
  translated: false,
  needswork: false,
  untranslated: false
}

const defaultState = {
  panels: {
    navHeader: {
      visible: true
    },
    sidebar: {
      visible: true
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
  textFlowDisplay: {
    filter: DEFAULT_FILTER_STATE
  },
  gettextCatalog: {
    getString: (key) => {
      // TODO pahuang implement gettextCatalog.getString
      // console.log('gettextCatalog.getString')
      return key
    }
  }
}

const ui = (state = defaultState, action) => {
  switch (action.type) {
    case SUGGESTION_PANEL_HEIGHT_CHANGE:
      return update({
        panels: {
          suggestions: {
            heightPercent: {$set: action.percentageHeight}
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
      const locales = prepareLocales(action.data)
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
          $set: action.data
        }
      })

    case RESET_STATUS_FILTERS:
      return update({
        textFlowDisplay: {
          filter: {
            $set: DEFAULT_FILTER_STATE
          }
        }
      })

    case UPDATE_STATUS_FILTER:
      const newFilter = {
        ...state.textFlowDisplay.filter,
        all: false,
        [action.status]: !state.textFlowDisplay.filter[action.status]
      }

      return update({
        textFlowDisplay: {
          filter: {
            $set: allStatusesSame(newFilter) ? DEFAULT_FILTER_STATE : newFilter
          }
        }
      })

    case SET_SIDEBAR_VISIBILITY:
      return update({
        panels: {
          sidebar: {
            visible: {$set: action.visible}
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

/**
 * Check if statuses are either all true or all false
 */
function allStatusesSame (statuses) {
  return statuses.approved === statuses.rejected &&
    statuses.rejected === statuses.translated &&
    statuses.translated === statuses.needswork &&
    statuses.needswork === statuses.untranslated
}

export default ui
