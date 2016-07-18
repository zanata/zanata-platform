import moment from 'moment'
// this import MUST come after import for moment
import 'moment-range'
import { isEmpty } from 'lodash'

var DateHelper = {
  dateFormat: 'YYYY-MM-DD',
  getDateRangeFromOption: function (dateRangeOption) {
    var now = moment()
    var dateFormat = this.dateFormat
    var dates = []
    var range
    var fromDate
    var toDate

    switch (dateRangeOption.value) {
      case 'thisWeek':
        fromDate = moment().weekday(0)
        toDate = moment().weekday(6)
        break
      case 'lastWeek':
        fromDate = moment().weekday(-7)
        toDate = moment().weekday(-1)
        break
      case 'thisMonth':
        fromDate = moment().date(1)
        toDate = moment().month(now.month() + 1).date(0)
        break
      case 'lastMonth':
        fromDate = moment().month(now.month() - 1).date(1)
        toDate = moment().date(0)
        break
      default:
        console.error('selectedDateRange [%s] can not be matched. ' +
          'Using (This Week) instead.', dateRangeOption)
        fromDate = moment().weekday(0)
        toDate = moment()
    }
    range = moment.range(fromDate, toDate)

    range.by('days', function (moment) {
      dates.push(moment.format(dateFormat))
    })

    return {
      fromDate: fromDate.format(dateFormat),
      toDate: toDate.format(dateFormat),
      dates: dates
    }
  },

  dayAsLabel: function (dateStr, numOfDays, useFullName) {
    var date = moment(dateStr)
    var dayOfWeekFmt
    var dayOfMonthFmt

    dayOfWeekFmt = useFullName ? 'dddd (Do MMM)' : 'ddd'
    dayOfMonthFmt = useFullName ? 'Do MMM (dddd)' : 'D'
    if (numOfDays < 8) {
      return date.format(dayOfWeekFmt)
    } else {
      return date.format(dayOfMonthFmt)
    }
  },

  isInFuture: function (dateStr) {
    return moment(dateStr).isAfter(moment())
  },

  getDate: function (milliseconds) {
    if (!isEmpty(milliseconds)) {
      const intMiliseconds = parseInt(milliseconds)
      return new Date(intMiliseconds)
    } else {
      return undefined
    }
  },

  shortDate: function (date) {
    if (date) {
      return moment(date).format('DD/MM/YYYY')
    } else {
      return undefined
    }
  }
}

export default DateHelper
