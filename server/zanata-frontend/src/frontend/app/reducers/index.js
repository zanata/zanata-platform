import { combineReducers } from 'redux'
import { routeReducer as routing } from 'react-router-redux'
import glossary from './glossary'
import explore from './explore'
import profile from './profile'
import common from './common'

const rootReducer = combineReducers({
  routing,
  explore,
  glossary,
  common,
  profile
})

export default rootReducer
