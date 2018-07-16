import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import { createSelector } from 'reselect'
import { has, keys, mapValues, omit, pick } from 'lodash'
import {
  SETTINGS_REQUEST,
  SETTINGS_SUCCESS,
  SETTINGS_FAILURE,
  SETTING_UPDATE,
  SETTINGS_SAVE_REQUEST,
  SETTINGS_SAVE_SUCCESS,
  SETTINGS_SAVE_FAILURE,
  VALIDATORS_SUCCESS
} from '../actions/settings-action-types'
import { SHORTCUTS } from '../actions/key-shortcuts-actions'

export const ENTER_SAVES_IMMEDIATELY = 'enter-saves-immediately'
export const SYNTAX_HIGHLIGTING = 'syntax-highlighting'
export const SUGGESTIONS_DIFF = 'suggestions-diff'
export const KEY_SUGGESTIONS_VISIBLE = 'suggestions-visible'

/* Validation Options */
export const HTML_XML = 'HTML_XML'
export const NEW_LINE = 'NEW_LINE'
export const TAB = 'TAB'
export const JAVA_VARIABLES = 'JAVA_VARIABLES'
export const XML_ENTITY = 'XML_ENTITY'
export const PRINTF_VARIABLES = 'PRINTF_VARIABLES'
export const PRINTF_XSI_EXTENSION = 'PRINTF_XSI_EXTENSION'

/** Parse values of known settings to appropriate types
 */
// @ts-ignore any
function parseKnownSettings (settings) {
  return mapValues(settings, (value, key) => {
    try {
      switch (key) {
        case ENTER_SAVES_IMMEDIATELY:
        case SYNTAX_HIGHLIGTING:
        case SUGGESTIONS_DIFF:
        case KEY_SUGGESTIONS_VISIBLE:
          return JSON.parse(value)
        default:
          return value
      }
    } catch (e) {
      console.error(`Failed parsing setting ${key} with value ${value}`)
    }
    return value
  })
}

/** convenience function to construct an empty setting body
 * @param value {any}
 */
const newSetting = value => ({ value, saving: false, error: undefined })

// Default validator value: one of ['Error','Warning','Off']
export const defaultValidation = 'Off'

/** @type {import('./state').EditorSettingsState} */
export const defaultState = {
  // state for all settings being requested on app load
  fetching: false,

  // error when attempt to load user settings fails
  error: undefined,

  // state for individual settings
  settings: {
    [ENTER_SAVES_IMMEDIATELY]: newSetting(false),
    [SYNTAX_HIGHLIGTING]: newSetting(false),
    [SUGGESTIONS_DIFF]: newSetting(true),
    [KEY_SUGGESTIONS_VISIBLE]: newSetting(true),
    // Validator options disabled by default
    [HTML_XML]: newSetting(defaultValidation),
    [NEW_LINE]: newSetting(defaultValidation),
    [TAB]: newSetting(defaultValidation),
    [JAVA_VARIABLES]: newSetting(defaultValidation),
    [XML_ENTITY]: newSetting(defaultValidation),
    [PRINTF_VARIABLES]: newSetting(defaultValidation),
    [PRINTF_XSI_EXTENSION]: newSetting(defaultValidation)
  }
}

/* Selectors */
/** @param settings {import('./state').EditorSettingsState} */
export const getSuggestionsPanelVisible = settings =>
  settings.settings[KEY_SUGGESTIONS_VISIBLE].value
/** @param settings {import('./state').EditorSettingsState} */
export const getEnterSavesImmediately = settings =>
  settings.settings[ENTER_SAVES_IMMEDIATELY].value
/** @param settings {import('./state').EditorSettingsState} */
export const getSyntaxHighlighting = settings =>
  settings.settings[SYNTAX_HIGHLIGTING].value
/** @param settings {import('./state').EditorSettingsState} */
export const getSuggestionsDiff = settings =>
  settings.settings[SUGGESTIONS_DIFF].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidateHtmlXml = settings =>
  settings.settings[HTML_XML].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidateNewLine = settings =>
  settings.settings[NEW_LINE].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidateTab = settings =>
  settings.settings[TAB].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidateJavaVariables = settings =>
  settings.settings[JAVA_VARIABLES].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidateXmlEntity = settings =>
  settings.settings[XML_ENTITY].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidatePrintfVariables = settings =>
  settings.settings[PRINTF_VARIABLES].value
/** @param settings {import('./state').EditorSettingsState} */
export const getValidatePrintfXsi = settings =>
  settings.settings[PRINTF_XSI_EXTENSION].value
export const getShortcuts = createSelector(getEnterSavesImmediately,
  enterSaves => enterSaves ? update(SHORTCUTS, {
    // Both shortcuts are at index 0, but replacing by value in case they move
    GOTO_NEXT_ROW_FAST: {
      keyConfig: {
        // @ts-ignore any
        keys: {$apply: keys => keys.map(v => v === 'mod+enter' ? 'enter' : v)}
      }
    }
  }) : SHORTCUTS)

export default handleActions({
  [SETTING_UPDATE]: (state, { payload }) => {
    // @ts-ignore
    const keys = Object.keys(payload)
    if (keys.length !== 1) {
      console.error(`expected object with exactly 1 key, but got: ${JSON.stringify(payload)}`) // eslint-disable-line max-len
      return state
    }
    const key = keys[0]
    // @ts-ignore
    const value = payload[key]
    if (!has(state.settings, key)) {
      console.error(`updating ${key}, but it is not in state to update`)
      return update(state, {
        settings: {
          // might as well create the value, the error does not prevent it
          [key]: {$set: {value, saving: false, error: undefined}}
        }
      })
    }
    return update(state, {
      settings: {
        [key]: { value: {$set: value} }
      }
    })
  },
  [SETTINGS_REQUEST]: state => update(state, { fetching: {$set: true} }),
  [SETTINGS_SUCCESS]: (state, { payload }) => {
    const defined = keys(state.settings)
    const parsed = parseKnownSettings(payload)
    const updates = mapValues(pick(parsed, defined),
      value => ({ value: {$set: value} }))
    // Added settings are probably unknown (no default value present) but I am
    // saving them to help with development and debugging.
    const additions = mapValues(omit(parsed, defined), value => ({$set: {
      value,
      saving: false,
      error: undefined
    }}))
    return update(state, {
      fetching: {$set: false},
      settings: {
        ...additions,
        ...updates
      }
    })
  },
  [VALIDATORS_SUCCESS]: (state, { payload }) => {
    const defined = keys(state.settings)
    const parsed = parseKnownSettings(payload)
    const updates = mapValues(pick(parsed, defined),
      value => ({ value: {$set: value} }))
    return update(state, {
      fetching: {$set: false},
      settings: {
        ...updates
      }
    })
  },
  [SETTINGS_FAILURE]: (state, { payload }) => update(state, {
    fetching: {$set: false},
    error: {$set: payload}
  }),
  // @ts-ignore meta
  [SETTINGS_SAVE_REQUEST]: (state, { meta: {settings} }) => update(state, {
    settings: mapValues(settings, _value => ({saving: {$set: true}}))
  }),
  // @ts-ignore meta
  [SETTINGS_SAVE_SUCCESS]: (state, { meta: {settings} }) => update(state, {
    settings: mapValues(settings, _value => ({
      saving: {$set: false},
      error: {$set: undefined}
    }))
  }),
  // @ts-ignore meta
  [SETTINGS_SAVE_FAILURE]: (state, { payload, meta }) => update(state, {
    settings: mapValues(meta.settings, _value => ({
      saving: {$set: false},
      error: {$set: payload}
    }))
  })
}, defaultState)
