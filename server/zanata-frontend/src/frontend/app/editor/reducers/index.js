import { combineReducers } from 'redux'
import { createSelector } from 'reselect'
import { routerReducer as routing } from 'react-router-redux'
import phrases from './phrase-reducer'
import context from './context-reducer'
import dropdown from './dropdown-reducer'
import glossary from './glossary-reducer'
import ui from './ui-reducer'
import headerData from './header-data-reducer'
import settings, * as settingsSelectors from './settings-reducer'
import suggestions from './suggestions-reducer'

const rootReducer = combineReducers({
  context,
  headerData,
  dropdown,
  glossary,
  phrases,
  routing,
  settings,
  suggestions,
  ui
})

export default rootReducer

/* Selectors for local parts of state are mapped to work on the top level */

export const getSettings = state => state.settings
export const getSuggestionsPanelVisible = createSelector(getSettings,
  settings => settingsSelectors.getSuggestionsPanelVisible(settings))
