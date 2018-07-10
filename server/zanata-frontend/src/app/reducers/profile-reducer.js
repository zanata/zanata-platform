// @ts-nocheck
import { handleActions } from 'redux-actions'
import {ContentStates} from '../constants/Options'
import utilsDate from '../utils/DateHelper'
import {
  USER_STATS_REQUEST,
  USER_STATS_SUCCESS,
  USER_STATS_FAILURE,
  DATE_RANGE_UPDATE,
  FILTER_UPDATE,
  SELECT_DAY_UPDATE,
  GET_LOCALE_REQUEST,
  GET_LOCALE_SUCCESS,
  GET_LOCALE_FAILURE
} from '../actions/profile-actions'
import {
  LOAD_USER_REQUEST,
  LOAD_USER_SUCCESS,
  LOAD_USER_FAILURE,
  SEVERITY,
  CLEAR_MESSAGE
} from '../actions/common-actions'
import { keyBy } from 'lodash'

/* eslint-enable max-len */
/**
 *
 * @param listOfMatrices original server response
 * @param {{fromDate: string, toDate: string, dates: string[]}} dateRange see
 *   DateHelper.getDateRangeFromOption(string)
 * @returns {{
     label: string, date: string, totalApproved: number,
     totalTranslated: number, totalNeedsWork: number,
     totalActivity: number
   }[]}
 */
/* eslint-disable max-len */
const transformToTotalWordCountsForEachDay = (listOfMatrices, dateRange) => {
  let result = []
  let index = 0

  dateRange.dates.forEach(function (dateStr) {
    let entry = listOfMatrices[index] || {}
    let totalApproved = 0
    let totalTranslated = 0
    let totalNeedsWork = 0

    while (entry.savedDate === dateStr) {
      switch (entry.savedState) {
        case 'Approved':
          totalApproved += entry.wordCount
          break
        case 'Translated':
          totalTranslated += entry.wordCount
          break
        case 'NeedReview':
          totalNeedsWork += entry.wordCount
          break
        default:
          throw new Error('unrecognized state:' + entry['savedState'])
      }
      index++
      entry = listOfMatrices[index] || {}
    }
    result.push({
      date: dateStr,
      totalApproved: totalApproved,
      totalTranslated: totalTranslated,
      totalNeedsWork: totalNeedsWork,
      totalActivity: totalApproved + totalNeedsWork + totalTranslated
    })
  })
  return result
}

const mapContentStateToFieldName = (selectedOption) => {
  switch (selectedOption) {
    case 'Total':
      return 'totalActivity'
    case 'Approved':
      return 'totalApproved'
    case 'Translated':
      return 'totalTranslated'
    case 'Needs Work':
      return 'totalNeedsWork'
  }
}

/**
 *
 * @param listOfMatrices this should be the result of
 *   transformToTotalWordCountsForEachDay().
 * @param {string} selectedContentState
 * @returns {{key: string, label: string, wordCount: number}[]}
 */
const mapTotalWordCountByContentState = (listOfMatrices,
                                         selectedContentState) => {
  const wordCountFieldName = mapContentStateToFieldName(selectedContentState)
  return listOfMatrices.map(function (entry) {
    return {
      date: entry.date,
      wordCount: entry[wordCountFieldName]
    }
  })
}

/**
 *
 * @param listOfMatrices original server response
 * @param {string} selectedContentState
 * @param {string?} selectedDay optional day
 * @return filtered entries in same form as original server response
 */
const filterByContentStateAndDay = (listOfMatrices, selectedContentState,
                                     selectedDay) => {
  let filteredEntries = listOfMatrices
  let predicates = []

  // we have messy terminologies!
  selectedContentState = (
    selectedContentState === 'Needs Work'
      ? 'NeedReview' : selectedContentState)

  if (selectedDay) {
    predicates.push(function (entry) {
      return entry.savedDate === selectedDay
    })
  }
  if (selectedContentState !== 'Total') {
    predicates.push(function (entry) {
      return entry.savedState === selectedContentState
    })
  }
  if (predicates.length > 0) {
    let predicate = function (entry) {
      return predicates.every(function (func) {
        return func.call({}, entry)
      })
    }
    filteredEntries = listOfMatrices.filter(predicate)
  }
  return filteredEntries
}

const processUserStatistics = (state, json) => {
  const wordCountsForEachDay =
    transformToTotalWordCountsForEachDay(json, state.dailyDateRange)
  const contentState = state.contentStateOption
  const selectedDay = state.selectedDay
  return {
    ...state,
    matrix: json,
    matrixForAllDays: wordCountsForEachDay,
    wordCountsForEachDayFilteredByContentState:
      mapTotalWordCountByContentState(wordCountsForEachDay, contentState),
    wordCountsForSelectedDayFilteredByContentState:
      filterByContentStateAndDay(json, contentState, selectedDay)
  }
}

export default handleActions({
  [CLEAR_MESSAGE]: (state, _action) => {
    return {
      ...state,
      notification: undefined
    }
  },
  [LOAD_USER_REQUEST]: (state, _action) => {
    return {
      ...state,
      user: {
        ...state.user,
        loading: true
      }
    }
  },
  [LOAD_USER_SUCCESS]: (state, action) => {
    const languageTeams =
      keyBy(action.payload.languageTeams, function (localeId) {
        return localeId
      })
    return {
      ...state,
      user: {
        ...action.payload,
        languageTeams: languageTeams
      }
    }
  },
  [LOAD_USER_FAILURE]: (state, _action) => {
    return {
      ...state,
      user: {
        ...state.user,
        loading: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load user info. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [USER_STATS_REQUEST]: (state, _action) => {
    return {
      ...state,
      loading: true,
      dailyDateRange: utilsDate.getDateRangeFromOption(state.dateRange)
    }
  },
  [USER_STATS_SUCCESS]: (state, action) => {
    const newState = processUserStatistics(state, action.payload)
    return {
      ...newState,
      loading: false
    }
  },
  [USER_STATS_FAILURE]: (state, _action) => {
    return {
      ...state,
      loading: false,
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load user statistics. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [DATE_RANGE_UPDATE]: (state, action) => {
    return {
      ...state,
      dateRange: action.payload
    }
  },
  [FILTER_UPDATE]: (state, action) => {
    const contentState = action.payload
    const {matrixForAllDays, matrix, selectedDay} = state
    return {
      ...state,
      contentStateOption: contentState,
      wordCountsForEachDayFilteredByContentState:
        mapTotalWordCountByContentState(matrixForAllDays, contentState),
      wordCountsForSelectedDayFilteredByContentState:
        filterByContentStateAndDay(matrix, contentState, selectedDay)
    }
  },
  [SELECT_DAY_UPDATE]: (state, action) => {
    const selectedDay = action.payload
    const {matrix, contentStateOption} = state
    return {
      ...state,
      selectedDay: selectedDay,
      wordCountsForSelectedDayFilteredByContentState:
        filterByContentStateAndDay(matrix, contentStateOption, selectedDay)
    }
  },
  [GET_LOCALE_REQUEST]: (state, _action) => {
    return {
      ...state,
      user: {
        ...state.user,
        loading: true
      }
    }
  },
  [GET_LOCALE_SUCCESS]: (state, action) => {
    const details = action.payload
    return {
      ...state,
      user: {
        ...state.user,
        loading: false,
        languageTeams: {
          ...state.user.languageTeams,
          [details.localeId]: details
        }
      }
    }
  },
  [GET_LOCALE_FAILURE]: (state, _action) => {
    return {
      ...state,
      loading: false,
      user: {
        username: '',
        loading: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable load user information. ' +
        'Please refresh this page and try again.'
      }
    }
  }
},
// default state
  /** @type {import('./state').ProfileState} */
  ({
    matrix: [],
    matrixForAllDays: [],
    wordCountsForEachDayFilteredByContentState: [],
    wordCountsForSelectedDayFilteredByContentState: [],
    selectedDay: null,
    contentStateOption: ContentStates[0],
    loading: false,
    dateRange: utilsDate.getDateRange('This week'),
    dailyDateRange:
      utilsDate.getDateRangeFromOption(utilsDate.getDateRange('This week')), // eslint-disable-line max-len
    user: {
      username: '',
      loading: false
    }
  }))
