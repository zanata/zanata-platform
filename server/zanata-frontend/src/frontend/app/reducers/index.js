import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import glossary from './glossary-reducer'
import explore from './explore-reducer'
import profile from './profile-reducer'
import common from './common-reducer'
import languages from './languages-reducer'
import tmx from './tmx-reducer'

const rootReducer = combineReducers({
  routing,
  explore,
  glossary,
  common,
  profile,
  languages,
  tmx
})

export default rootReducer
