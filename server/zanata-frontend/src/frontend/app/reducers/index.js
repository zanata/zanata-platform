import { combineReducers } from 'redux'
import { routeReducer as routing } from 'react-router-redux'
import glossary from './glossary-reducer'
import explore from './explore-reducer'
import profile from './profile-reducer'
import common from './common-reducer'
import languages from './languages-reducer'

const rootReducer = combineReducers({
  routing,
  explore,
  glossary,
  common,
  profile,
  languages
})

export default rootReducer
