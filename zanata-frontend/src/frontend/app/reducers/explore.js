import { handleActions } from 'redux-actions'
import { cloneDeep } from 'lodash'
import {
  SEARCH_PROJECT_REQUEST,
  SEARCH_PROJECT_SUCCESS,
  SEARCH_PROJECT_FAILURE,
  SEARCH_LANG_TEAM_REQUEST,
  SEARCH_LANG_TEAM_SUCCESS,
  SEARCH_LANG_TEAM_FAILURE,
  SEARCH_PEOPLE_REQUEST,
  SEARCH_PEOPLE_SUCCESS,
  SEARCH_PEOPLE_FAILURE,
  SEARCH_GROUP_REQUEST,
  SEARCH_GROUP_SUCCESS,
  SEARCH_GROUP_FAILURE
} from '../actions/explore'

export default handleActions({
  [SEARCH_PROJECT_REQUEST]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Project = true
    return {
      ...newState
    }
  },
  [SEARCH_PROJECT_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Project = false
    newState.results.Project = action.payload
    return {
      ...newState
    }
  },
  [SEARCH_PROJECT_FAILURE]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Project = false
    newState.results.Project = action.payload
    newState.error = true
    return {
      ...newState
    }
  },
  [SEARCH_LANG_TEAM_REQUEST]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.LanguageTeam = true
    return {
      ...newState
    }
  },
  [SEARCH_LANG_TEAM_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.LanguageTeam = false
    newState.results.LanguageTeam = action.payload
    return {
      ...newState
    }
  },
  [SEARCH_LANG_TEAM_FAILURE]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.LanguageTeam = false
    newState.results.LanguageTeam = action.payload
    newState.error = true
    return {
      ...newState
    }
  },
  [SEARCH_PEOPLE_REQUEST]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Person = true
    return {
      ...newState
    }
  },
  [SEARCH_PEOPLE_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Person = false
    newState.results.Person = action.payload
    return {
      ...newState
    }
  },
  [SEARCH_PEOPLE_FAILURE]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Person = false
    newState.results.Person = action.payload
    newState.error = true
    return {
      ...newState
    }
  },
  [SEARCH_GROUP_REQUEST]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Group = true
    return {
      ...newState
    }
  },
  [SEARCH_GROUP_SUCCESS]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Group = false
    newState.results.Group = action.payload
    return {
      ...newState
    }
  },
  [SEARCH_GROUP_FAILURE]: (state, action) => {
    let newState = cloneDeep(state)
    newState.loading.Group = false
    newState.results.Group = action.payload
    newState.error = true
    return {
      ...newState
    }
  }
},
// default state
  {
    error: false,
    loading: {
      Project: false,
      LanguageTeam: false,
      Person: false,
      Group: false
    },
    results: {}
  })
