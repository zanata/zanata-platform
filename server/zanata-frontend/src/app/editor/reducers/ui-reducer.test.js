/* global jest describe it expect */

import { SET_SIDEBAR_VISIBILITY } from '../actions/action-types'
import {
  CHANGE_UI_LOCALE,
  TOGGLE_GLOSSARY,
  TOGGLE_ACTIVITY,
  TOGGLE_HEADER,
  TOGGLE_KEY_SHORTCUTS,
  UI_LOCALES_FETCHED
} from '../actions/header-action-types'
import {
  SUGGESTION_PANEL_HEIGHT_CHANGE
} from '../actions/suggestions-action-types'

import uiReducer, { GLOSSARY_TAB, ACTIVITY_TAB, identity } from './ui-reducer'

describe('ui-reducer test', () => {
  it('can pass test', () => {
    expect(true).toEqual(true)
  })
  it('generates initial state', () => {
    // @ts-ignore
    const initialState = uiReducer(undefined, {})
    expect(initialState).toEqual({
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
      uiLocales: {},
      selectedUiLocale: 'en-US',
      showSettings: false,
      gettextCatalog: {
        getString: identity
      }
    })
  })

  it('can set sidebar visibility', () => {
    // @ts-ignore
    const visible = uiReducer(undefined, {
      type: SET_SIDEBAR_VISIBILITY,
      payload: true
    })
    // @ts-ignore
    const invisible = uiReducer(visible, {
      type: SET_SIDEBAR_VISIBILITY,
      payload: false
    })
    expect(visible.panels.sidebar.visible).toEqual(true)
    expect(invisible.panels.sidebar.visible).toEqual(false)
  })

  it('can change UI locale', () => {
    // @ts-ignore
    const withLocale = uiReducer(undefined, {
      type: CHANGE_UI_LOCALE,
      payload: 'jp'
    })
    expect(withLocale.selectedUiLocale).toEqual('jp')
  })

  it('can toggle glossary', () => {
    const openedGlossary = uiReducer(undefined, { type: TOGGLE_GLOSSARY })
    const closedGlossary = uiReducer(openedGlossary, { type: TOGGLE_GLOSSARY })
    expect(closedGlossary.panels.sidebar).toEqual({
      visible: true,
      selectedTab: ACTIVITY_TAB
    })
    expect(openedGlossary.panels.sidebar).toEqual({
      visible: true,
      selectedTab: GLOSSARY_TAB
    })
  })

  it('can toggle activity', () => {
    const closedActivity = uiReducer(undefined, { type: TOGGLE_ACTIVITY })
    const openedActivity = uiReducer(closedActivity, { type: TOGGLE_ACTIVITY })
    expect(closedActivity.panels.sidebar).toEqual({
      visible: true,
      selectedTab: GLOSSARY_TAB
    })
    expect(openedActivity.panels.sidebar).toEqual({
      visible: true,
      selectedTab: ACTIVITY_TAB
    })
  })

  it('can toggle header', () => {
    const closedHeader = uiReducer(undefined, { type: TOGGLE_HEADER })
    const openedHeader = uiReducer(closedHeader, { type: TOGGLE_HEADER })
    expect(closedHeader.panels.navHeader.visible).toEqual(false)
    expect(openedHeader.panels.navHeader.visible).toEqual(true)
  })

  it('can toggle key shortcuts modal', () => {
    const openedKeyShortcuts = uiReducer(undefined, {
      type: TOGGLE_KEY_SHORTCUTS
    })
    const closedKeyShortcuts = uiReducer(openedKeyShortcuts, {
      type: TOGGLE_KEY_SHORTCUTS
    })
    expect(openedKeyShortcuts.panels.keyShortcuts.visible).toEqual(true)
    expect(closedKeyShortcuts.panels.keyShortcuts.visible).toEqual(false)
  })

  it('can record fetched UI locales', () => {
    // @ts-ignore
    const withUiLocales = uiReducer(undefined, {
      type: UI_LOCALES_FETCHED,
      payload: [
        {
          localeId: 'en-US',
          displayName: 'English (United States)'
        },
        {
          localeId: 'de',
          displayName: 'German',
          pluralForms: 'nplurals=2; plural=(n != 1)'
        },
        {
          localeId: 'ja',
          displayName: 'Japanese',
          pluralForms: 'nplurals=1; plural=0'
        }
      ]
    })
    expect(withUiLocales.uiLocales).toEqual({
      'en-US': {
        id: 'en-US',
        name: 'English (United States)'
      },
      de: {
        id: 'de',
        name: 'German',
        nplurals: 2
      },
      ja: {
        id: 'ja',
        name: 'Japanese',
        nplurals: 1
      }
    })
  })

  it('can record suggestion panel height change', () => {
    // @ts-ignore
    const changedHeight = uiReducer(undefined, {
      type: SUGGESTION_PANEL_HEIGHT_CHANGE,
      payload: 0.4
    })
    expect(changedHeight.panels.suggestions.heightPercent).toEqual(0.4)
  })
})
