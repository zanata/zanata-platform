import { combineReducers } from 'redux'
import { createSelector } from 'reselect'
import { routerReducer as routing } from 'react-router-redux'
import phrases from './phrase-reducer'
import context from './context-reducer'
import dropdown from './dropdown-reducer'
import glossary from './glossary-reducer'
import ui, * as uiSelectors from './ui-reducer'
import headerData from './header-data-reducer'
import settings, * as settingsSelectors from './settings-reducer'
import suggestions from './suggestions-reducer'
import review from './review-trans-reducer'

const rootReducer = combineReducers({
  context,
  headerData,
  dropdown,
  glossary,
  phrases,
  routing,
  settings,
  suggestions,
  review,
  ui
})

export default rootReducer

/* Selectors for local parts of state are mapped to work on the top level */

/* settings */
export const getSettings = state => state.settings
export const getSuggestionsPanelVisible = createSelector(
  getSettings, settingsSelectors.getSuggestionsPanelVisible)
export const getEnterSavesImmediately = createSelector(
  getSettings, settingsSelectors.getEnterSavesImmediately)
export const getSyntaxHighlighting = createSelector(
  getSettings, settingsSelectors.getSyntaxHighlighting)
export const getSuggestionsDiff = createSelector(
  getSettings, settingsSelectors.getSuggestionsDiff)
export const getShortcuts = createSelector(
  getSettings, settingsSelectors.getShortcuts)
export const getValidateHtmlXml = createSelector(
  getSettings, settingsSelectors.getValidateHtmlXml)
export const getValidateNewLine = createSelector(
  getSettings, settingsSelectors.getValidateNewLine)
export const getValidateTab = createSelector(
  getSettings, settingsSelectors.getValidateTab)
export const getValidateJavaVariables = createSelector(
  getSettings, settingsSelectors.getValidateJavaVariables)
export const getValidateXmlEntity = createSelector(
  getSettings, settingsSelectors.getValidateXmlEntity)
export const getValidatePrintfVariables = createSelector(
  getSettings, settingsSelectors.getValidatePrintfVariables)
export const getValidatePrintfXsi = createSelector(
  getSettings, settingsSelectors.getValidatePrintfXsi)

/* ui */
export const getUi = state => state.ui
export const getNavHeaderVisible = createSelector(
  getUi, uiSelectors.getNavHeaderVisible)
export const getSidebarVisible = createSelector(
  getUi, uiSelectors.getSidebarVisible)
export const getSidebarTab = createSelector(getUi, uiSelectors.getSidebarTab)
export const getShowSettings = createSelector(
  getUi, uiSelectors.getShowSettings)
export const getGlossaryVisible = createSelector(
  getUi, uiSelectors.getGlossaryVisible)
export const getInfoPanelVisible = createSelector(
  getUi, uiSelectors.getInfoPanelVisible)
export const getKeyShortcutsVisible = createSelector(
  getUi, uiSelectors.getKeyShortcutsVisible)
export const getAppLocale = state => state.headerData.localeMessages
