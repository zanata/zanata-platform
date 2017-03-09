
import { combineReducers } from 'redux'
// TODO update dependency, it is renamed react-router-redux
import { routeReducer } from 'redux-simple-router'
import phrases from './phrase'
import context from './context'
import dropdown from './dropdown'
import ui from './ui'
import headerData from './headerData'
import suggestions from './suggestions'

const rootReducer = combineReducers({
  context,
  headerData,
  dropdown,
  phrases,
  routing: routeReducer,
  suggestions,
  ui
})

export default rootReducer
