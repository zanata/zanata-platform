import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import { isEmpty, includes } from 'lodash'
import utilsDate from '../utils/DateHelper'

import {
  getJsonHeaders,
  buildAPIRequest
} from './common'

export const FILTER_UPDATE = 'FILTER_UPDATE'
export const DATE_RANGE_UPDATE = 'DATE_RANGE_UPDATE'
export const SELECT_DAY_UPDATE = 'SELECT_DAY_UPDATE'

export const LOAD_USER_REQUEST = 'LOAD_USER_REQUEST'
export const LOAD_USER_SUCCESS = 'LOAD_USER_SUCCESS'
export const LOAD_USER_FAILURE = 'LOAD_USER_FAILURE'

export const USER_STATS_REQUEST = 'USER_STATS_REQUEST'
export const USER_STATS_SUCCESS = 'USER_STATS_SUCCESS'
export const USER_STATS_FAILURE = 'USER_STATS_FAILURE'

export const updateDateRange = createAction(DATE_RANGE_UPDATE)
export const updateFilter = createAction(FILTER_UPDATE)
export const updateSelectDay = createAction(SELECT_DAY_UPDATE)

const getStatsEndPoint = (username, fromDate, toDate) => {
  return window.config.baseUrl + window.config.apiRoot +
    '/stats/user/' + username + '/' + fromDate + '..' + toDate
}

const getUserStatistics = (username, fromDate, toDate) => {
  const endpoint = getStatsEndPoint(username, fromDate, toDate)
  const apiTypes = [
    USER_STATS_REQUEST,
    {
      type: USER_STATS_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    USER_STATS_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const loadUserStats = (username, dateRangeOption) => {
  return (dispatch, getState) => {
    const dateRange = utilsDate.getDateRangeFromOption(dateRangeOption)
    dispatch(getUserStatistics(username, dateRange.fromDate, dateRange.toDate))
  }
}

const getUserInfo = (dispatch, username, dateRangeOption) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot + '/user' +
    (isEmpty(username) ? '' : '/' + username)

  const apiTypes = [
    LOAD_USER_REQUEST,
    {
      type: LOAD_USER_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            dispatch(loadUserStats(username, dateRangeOption))
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    LOAD_USER_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

export const profileInitialLoad = (username) => {
  return (dispatch, getState) => {
    const config = window.config
    if (isEmpty(username) && !config.permission.isLoggedIn) {
      // redirect to login screen if no username is found url
      // and user is not logged in
      window.location = config.baseUrl + config.links.loginUrl + '#profile'
    } else {
      dispatch(getUserInfo(dispatch, username,
        getState().profile.dateRangeOption))
    }
  }
}

export const dateRangeChanged = (dataRangeOption) => {
  return (dispatch, getState) => {
    const username = getState().profile.user.username
    dispatch(updateDateRange(dataRangeOption))
    dispatch(loadUserStats(username, dataRangeOption))
  }
}

export const filterUpdate = (contentState) => {
  return (dispatch, getState) => {
    if (getState().profile.contentStateOption !== contentState) {
      dispatch(updateFilter(contentState))
    }
  }
}

export const selectDayChanged = (day) => {
  return (dispatch, getState) => {
    // click the same day again will cancel selection
    const selectedDay = getState().profile.selectedDay !== day ? day : null
    dispatch(updateSelectDay(selectedDay))
  }
}
