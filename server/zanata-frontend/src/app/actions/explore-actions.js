import { CALL_API } from 'redux-api-middleware'
import { replaceRouteQuery } from '../utils/RoutingHelpers'
import {
  getJsonHeaders,
  buildAPIRequest,
  // eslint-disable-next-line
  APITypes
} from './common-actions'
import { isEmpty, includes, clamp } from 'lodash'
import { apiUrl } from '../config'

export const SEARCH_PROJECT_REQUEST = 'SEARCH_PROJECT_REQUEST'
export const SEARCH_PROJECT_SUCCESS = 'SEARCH_PROJECT_SUCCESS'
export const SEARCH_PROJECT_FAILURE = 'SEARCH_PROJECT_FAILURE'

export const SEARCH_LANG_TEAM_REQUEST = 'SEARCH_LANG_TEAM_REQUEST'
export const SEARCH_LANG_TEAM_SUCCESS = 'SEARCH_LANG_TEAM_SUCCESS'
export const SEARCH_LANG_TEAM_FAILURE = 'SEARCH_LANG_TEAM_FAILURE'

export const SEARCH_PEOPLE_REQUEST = 'SEARCH_PEOPLE_REQUEST'
export const SEARCH_PEOPLE_SUCCESS = 'SEARCH_PEOPLE_SUCCESS'
export const SEARCH_PEOPLE_FAILURE = 'SEARCH_PEOPLE_FAILURE'

export const SEARCH_GROUP_REQUEST = 'SEARCH_GROUP_REQUEST'
export const SEARCH_GROUP_SUCCESS = 'SEARCH_GROUP_SUCCESS'
export const SEARCH_GROUP_FAILURE = 'SEARCH_GROUP_FAILURE'

export const SIZE_PER_PAGE = 20

// @ts-ignore any
const getEndpoint = (type, page, searchText) => {
  return apiUrl + '/search/' +
    type + '?' +
    'sizePerPage=' + SIZE_PER_PAGE +
    '&page=' + (page || '1') +
    (searchText ? '&q=' + encodeURIComponent(searchText) : '')
}

// @ts-ignore any
const handleCallbacks = (callbacks, dispatch, searchText, pages) => {
  if (callbacks) {
    const callback = callbacks[0]
    dispatch(callback(dispatch, searchText, pages,
      callbacks.splice(1)))
  }
}

// @ts-ignore any
const getSearchProjectResults = (dispatch, searchText, pages, callbacks) => {
  const endpoint = getEndpoint('projects', pages.projectPage, searchText)
  /** @type {APITypes} */
  const apiTypes = [
    SEARCH_PROJECT_REQUEST,
    {
      type: SEARCH_PROJECT_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            !isEmpty(callbacks) &&
            handleCallbacks(callbacks, dispatch, searchText, pages)
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    SEARCH_PROJECT_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getSearchLanguageTeamResults = (dispatch, searchText,
                                      // @ts-ignore any
                                      pages, callbacks) => {
  const endpoint =
    getEndpoint('teams/language', pages.languageTeamPage, searchText)
  /** @type {APITypes} */
  const apiTypes = [
    SEARCH_LANG_TEAM_REQUEST,
    {
      type: SEARCH_LANG_TEAM_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            !isEmpty(callbacks) &&
            handleCallbacks(callbacks, dispatch, searchText, pages)
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    SEARCH_LANG_TEAM_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getSearchPeopleResults = (dispatch, searchText, pages, callbacks) => {
  const endpoint = getEndpoint('people', pages.personPage, searchText)
  /** @type {APITypes} */
  const apiTypes = [
    SEARCH_PEOPLE_REQUEST,
    {
      type: SEARCH_PEOPLE_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            !isEmpty(callbacks) &&
            handleCallbacks(callbacks, dispatch, searchText, pages)
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    SEARCH_PEOPLE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const getSearchGroupResults = (dispatch, searchText, pages, callbacks) => {
  const endpoint = getEndpoint('groups', pages.groupPage, searchText)
  /** @type {APITypes} */
  const apiTypes = [
    SEARCH_GROUP_REQUEST,
    {
      type: SEARCH_GROUP_SUCCESS,
      // @ts-ignore any
      payload: (_action, _state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          // @ts-ignore any
          return res.json().then((json) => {
            !isEmpty(callbacks) &&
            handleCallbacks(callbacks, dispatch, searchText, pages)
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    SEARCH_GROUP_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

// @ts-ignore any
const search = (dispatch, searchText, pages) => {
  if (searchText) {
    dispatch(getSearchProjectResults(dispatch, searchText, pages,
      [getSearchGroupResults, getSearchLanguageTeamResults,
        getSearchPeopleResults]))
  } else {
    dispatch(getSearchProjectResults(dispatch, searchText, pages,
      [getSearchGroupResults]))
  }
}

const queryPageType = {
  'Project': 'projectPage',
  'Group': 'groupPage',
  'Person': 'personPage',
  'LanguageTeam': 'languageTeamPage'
}

// @ts-ignore any
export const searchTextChanged = (searchText) => {
  // @ts-ignore any
  return (dispatch, getState) => {
    if (getState().routing.locationBeforeTransitions !== searchText) {
      replaceRouteQuery(getState().routing.locationBeforeTransitions, {
        q: searchText,
        projectPage: null,
        groupPage: null,
        personPage: null,
        languageTeamPage: null
      })
      const query = getState().routing.locationBeforeTransitions.query
      const projectPage = query.projectPage
      const groupPage = query.groupPage
      const personPage = query.personPage
      const languageTeamPage = query.languageTeamPage

      search(dispatch, searchText, {projectPage,
        groupPage, personPage, languageTeamPage})
    }
  }
}

// @ts-ignore any
export const updateSearchPage = (type, currentPage, totalPage, next) => {
  const intCurrentPage = parseInt(currentPage)
  const adjustment = next ? 1 : -1
  const newPage = clamp(intCurrentPage + adjustment, 1, totalPage)

  const pageType = queryPageType[type]
  // @ts-ignore any
  return (dispatch, getState) => {
    let queryObj = {}
    queryObj[pageType] = newPage
    replaceRouteQuery(getState().routing.locationBeforeTransitions, queryObj)
    const query = getState().routing.locationBeforeTransitions.query

    const searchText = query.q
    const { projectPage, groupPage, personPage, languageTeamPage } = query

    switch (type) {
      case 'Project':
        dispatch(getSearchProjectResults(dispatch, searchText,
          {projectPage: newPage, groupPage, personPage, languageTeamPage}))
        break
      case 'Group':
        dispatch(getSearchGroupResults(dispatch, searchText,
          {projectPage, groupPage: newPage, personPage, languageTeamPage}))
        break
      case 'Person':
        dispatch(getSearchPeopleResults(dispatch, searchText,
          {projectPage, groupPage, personPage: newPage, languageTeamPage}))
        break
      case 'LanguageTeam':
        dispatch(getSearchLanguageTeamResults(dispatch, searchText,
          {projectPage, groupPage, personPage, languageTeamPage: newPage}))
        break
      default:
        console.error('Unsupported type in search query.', type)
        break
    }
  }
}

export const searchPageInitialLoad = () => {
  // @ts-ignore any
  return (dispatch, getState) => {
    const query = getState().routing.locationBeforeTransitions.query
    const searchText = query.q
    const { projectPage, groupPage, personPage, languageTeamPage } = query
    search(dispatch, searchText, {projectPage,
      groupPage, personPage, languageTeamPage})
  }
}
