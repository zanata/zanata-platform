import Dispatcher from '../dispatchers/UserMatrixDispatcher'
import assign from 'object-assign'
import {Promise} from 'es6-promise'
import {EventEmitter} from 'events'
import {ContentStates, DateRanges} from '../constants/Options'
import {UserMatrixActionTypes} from '../constants/ActionTypes'
import utilsDate from '../utils/DateHelper'
import Request from 'superagent'

const CHANGE_EVENT = 'change'

let _state = {
  matrix: [],
  matrixForAllDays: [],
  wordCountsForEachDayFilteredByContentState: [],
  wordCountsForSelectedDayFilteredByContentState: [],
  dateRangeOption: DateRanges[0],
  selectedDay: null,
  contentStateOption: ContentStates[0],
  loading: false,
  dateRange: function (option) {
    return utilsDate.getDateRangeFromOption(option)
  },
  user: {
    username: '',
    loading: false
  }
}

const loadUserInfo = (username) => {
  _state.user.loading = true
  UserMatrixStore.emitChange()

  const url = window.config.baseUrl + window.config.apiRoot +
    '/user/' + username
  return new Promise(function (resolve, reject) {
    // we turn off cache because it seems like if server(maybe just node?)
    // returns 304 unmodified, it won't even reach the callback!
    Request.get(url)
      .set('Cache-Control', 'no-cache, no-store, must-revalidate')
      .set('Pragma', 'no-cache')
      .set('Expires', 0)
      .set('Accept', 'application/json')
      .end(function (err, res) {
        err ? reject(err) : resolve(res.body)
        _state.user.loading = false
      })
  })
}

function loadFromServer () {
  _state.loading = true
  UserMatrixStore.emitChange()

  var dateRangeOption = _state.dateRangeOption
  var dateRange = utilsDate.getDateRangeFromOption(dateRangeOption)
  var url = statsAPIUrl() + dateRange.fromDate + '..' + dateRange.toDate

  _state.dateRange = dateRange
  return new Promise(function (resolve, reject) {
    // we turn off cache because it seems like if server(maybe just node?)
    // returns 304 unmodified, it won't even reach the callback!
    Request.get(url)
      .set('Cache-Control', 'no-cache, no-store, must-revalidate')
      .set('Pragma', 'no-cache')
      .set('Expires', 0)
      .end(function (err, res) {
        err ? reject(err) : resolve(res.body)
        _state.loading = false
      })
  })
}

function statsAPIUrl () {
  const postFix = window.config.dev ? '.json?' : ''
  return window.config.baseUrl + window.config.apiRoot +
    '/stats/user/' + _state.user.username + postFix + '/'
}

function handleServerResponse (serverResponse) {
  var dateRange = _state.dateRange
  var wordCountsForEachDay =
      transformToTotalWordCountsForEachDay(serverResponse, dateRange)
  var contentState = _state.contentStateOption
  var selectedDay = _state.selectedDay

  _state.matrix = serverResponse
  _state.matrixForAllDays = wordCountsForEachDay
  _state.wordCountsForEachDayFilteredByContentState =
    mapTotalWordCountByContentState(wordCountsForEachDay, contentState)
  _state.wordCountsForSelectedDayFilteredByContentState =
    filterByContentStateAndDay(_state.matrix, contentState, selectedDay)
  return _state
}

/**
 *
 * @param listOfMatrices original server response
 * @param {{fromDate: string, toDate: string, dates: string[]}} dateRange see
 *   DateHelper.getDateRangeFromOption(string)
 * @returns {{label: string, date: string, totalApproved: number,
   *   totalTranslated: number, totalNeedsWork: number, totalActivity:
   *   number}[]}
 */
function transformToTotalWordCountsForEachDay (listOfMatrices, dateRange) {
  var datesOfThisPeriod = dateRange.dates
  var result = []
  var index = 0

  datesOfThisPeriod.forEach(function (dateStr) {
    var entry = listOfMatrices[index] || {}
    var totalApproved = 0
    var totalTranslated = 0
    var totalNeedsWork = 0

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

    result.push(
      {
        date: dateStr,
        totalApproved: totalApproved,
        totalTranslated: totalTranslated,
        totalNeedsWork: totalNeedsWork,
        totalActivity: totalApproved + totalNeedsWork + totalTranslated
      })
  })

  return result
}

function mapContentStateToFieldName (selectedOption) {
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
function mapTotalWordCountByContentState (listOfMatrices,
                                          selectedContentState) {
  var wordCountFieldName = mapContentStateToFieldName(selectedContentState)
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
function filterByContentStateAndDay (listOfMatrices, selectedContentState,
  selectedDay) {
  var filteredEntries = listOfMatrices
  var predicates = []
  var predicate

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
    predicate = function (entry) {
      return predicates.every(function (func) {
        return func.call({}, entry)
      })
    }
    filteredEntries = listOfMatrices.filter(predicate)
  }
  return filteredEntries
}

/**
 * Stores that handle user profile page.
 * See containers/UserProfile/index.jsx for usage.
 *
 * TODO: convert to redux format
 */
var UserMatrixStore = assign({}, EventEmitter.prototype, {
  getMatrixState: function (username) {
    if (_state.user && _state.user.username !== username) {
      _state.user.username = username
      loadUserInfo(username).then(function (userInfo) {
        _state.user = userInfo
        UserMatrixStore.emitChange()
      })
    }
    if ((_state.matrixForAllDays.length === 0 &&
      window.config.permission.isLoggedIn)) {
      loadFromServer()
      .then(handleServerResponse)
      .then(function (newState) {
        UserMatrixStore.emitChange()
      })
    }
    return _state
  },
  emitChange: function () {
    this.emit(CHANGE_EVENT)
  },
  /**
   * @param {function} callback
   */
  addChangeListener: function (callback) {
    this.on(CHANGE_EVENT, callback)
  },
  /**
   * @param {function} callback
   */
  removeChangeListener: function (callback) {
    this.removeListener(CHANGE_EVENT, callback)
  },
  dispatchToken: Dispatcher.register(function (payload) {
    var action = payload.action
    switch (action.actionType) {
      case UserMatrixActionTypes.DATE_RANGE_UPDATE:
        _state.dateRangeOption = action.data
        _state.selectedDay = null
        if (window.config.permission.isLoggedIn) {
          loadFromServer()
            .then(handleServerResponse)
            .then(function (newState) {
              UserMatrixStore.emitChange()
            })
            .catch(function (err) {
              console.error(
                'failed trying to load user statistic from server' + err.stack)
            })
        }
        break
      case UserMatrixActionTypes.CONTENT_STATE_UPDATE:
        _state.contentStateOption = action.data
        _state.wordCountsForEachDayFilteredByContentState =
          mapTotalWordCountByContentState(_state.matrixForAllDays,
            _state.contentStateOption)
        _state.wordCountsForSelectedDayFilteredByContentState =
          filterByContentStateAndDay(_state.matrix,
            _state.contentStateOption, _state.selectedDay)
        UserMatrixStore.emitChange()
        break
      case UserMatrixActionTypes.DAY_SELECTED:
        _state.selectedDay = action.data
        _state.wordCountsForSelectedDayFilteredByContentState =
          filterByContentStateAndDay(_state.matrix,
            _state.contentStateOption, _state.selectedDay)
        UserMatrixStore.emitChange()
        break
    }
  })
})

export default UserMatrixStore
