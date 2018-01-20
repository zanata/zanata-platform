const moment /* TS: import moment */ = require('moment')
// this import MUST come after import for moment
import 'moment-range'
import { isEmpty, findKey } from 'lodash'

var DateHelper = {
  shortDateFormat: 'DD/MM/YYYY',
  shortDateTimeFormat: 'DD/MM/YYYY HH:mm',
  dateFormat: 'YYYY-MM-DD',
  dateRangeDisplayFmt: 'DD MMM, YYYY',
  dateSingleDisplayFmt: 'DD MMM, YYYY (dddd)',
  getDateRangeFromOption: function (dateRange) {
    const dateFormat = this.dateFormat
    const fromDate = dateRange.startDate
    const toDate = dateRange.endDate
    var dates = []
    const range = moment.range(fromDate, toDate)

    range.by('days', function (moment) {
      dates.push(moment.format(dateFormat))
    })

    return {
      fromDate: fromDate.format(dateFormat),
      toDate: toDate.format(dateFormat),
      dates: dates
    }
  },

  dayAsLabel: function (dateStr, numOfDays) {
    const date = moment(dateStr)
    const dayOfWeekFmt = 'ddd'
    const dayOfMonthFmt = 'D/MM'

    if (numOfDays < 8) {
      return date.format(dayOfWeekFmt)
    } else if (numOfDays >= 8 && numOfDays < 32) {
      return date.format(dayOfMonthFmt)
    } else {
      if (date.date() === 1) {
        return date.startOf('month').format('D/MMM')
      } else {
        return ''
      }
    }
  },

  isInFuture: function (dateStr) {
    return moment(dateStr).isAfter(moment())
  },

  getDate: function (milliseconds) {
    if (!isEmpty(milliseconds)) {
      return new Date(parseInt(milliseconds))
    } else {
      return undefined
    }
  },

  formatDate: function (date, format) {
    return moment(date).format(format)
  },

  shortDate: function (date) {
    if (date) {
      return this.formatDate(date, this.shortDateFormat)
    } else {
      return undefined
    }
  },

  shortDateTime: function (date) {
    if (date) {
      return this.formatDate(date, this.shortDateTimeFormat)
    } else {
      return undefined
    }
  },

  /**
   * Calculate days range between startDate and endDate,
   * if more than given days, it will adjust the endDate
   *
   * @days - days different to check. Must be 0 or more.
   * returns {startDate: startDate, endDate: adjustedEndDate}
   */
  keepDateInRange: function (startDate, endDate, days) {
    if (days < 0) {
      console.error('Days must be more 0 or more.', days)
    }
    const range = moment.range(startDate, endDate)
    const adjustedEndDate = range.diff('days') >= days
      ? moment(startDate).days(startDate.days() + (days - 1))
      : endDate
    return {
      startDate,
      endDate: adjustedEndDate
    }
  },

  getDefaultDateRange: function () {
    return {
      'This week': {
        startDate: function startDate (now) {
          return moment().weekday(0)
        },
        endDate: function endDate (now) {
          return moment().weekday(6)
        }
      },
      'Last week': {
        startDate: function startDate (now) {
          return moment().weekday(-7)
        },
        endDate: function endDate (now) {
          return moment().weekday(-1)
        }
      },
      'This month': {
        startDate: function startDate (now) {
          return moment().date(1)
        },
        endDate: function endDate (now) {
          return moment().month(now.month() + 1).date(0)
        }
      },
      'Last month': {
        startDate: function startDate (now) {
          return moment().month(now.month() - 1).date(1)
        },
        endDate: function endDate (now) {
          return moment().date(0)
        }
      },
      'This year': {
        startDate: function startDate (now) {
          return moment().year(now.year()).month(0).date(1)
        },
        endDate: function endDate (now) {
          return moment().year(now.year()).month(11).date(31)
        }
      }
    }
  },

  getDateRange: function (option) {
    const dateRange = this.getDefaultDateRange()[option]
    return {
      startDate: dateRange.startDate(),
      endDate: dateRange.endDate()
    }
  },

  getDateRangeLabel: function (dateRange) {
    const now = moment()
    return findKey(this.getDefaultDateRange(), function (range) {
      return moment(range.startDate(now)).isSame(dateRange.startDate, 'day') &&
        moment(range.endDate(now)).isSame(dateRange.endDate, 'day')
    })
  }
}

export default DateHelper
