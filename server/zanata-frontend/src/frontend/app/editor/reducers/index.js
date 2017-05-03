import { combineReducers } from 'redux'
// TODO update dependency, it is renamed react-router-redux
import { routeReducer } from 'redux-simple-router'
import phrases from './phrase-reducer'
import context from './context-reducer'
import dropdown from './dropdown-reducer'
import glossary from './glossary-reducer'
import ui from './ui-reducer'
import headerData from './header-data-reducer'
import suggestions from './suggestions-reducer'

const rootReducer = combineReducers({
  context,
  headerData,
  dropdown,
  glossary,
  phrases,
  routing: routeReducer,
  suggestions,
  ui
})

export default rootReducer
