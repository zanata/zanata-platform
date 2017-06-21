import { combineReducers } from 'redux'
import { routerReducer } from 'react-router-redux'
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
  routing: routerReducer,
  suggestions,
  ui
})

export default rootReducer
