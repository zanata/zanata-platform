import { extendMoment, MomentRangeExtends } from 'moment-range'
import { isEmpty, findKey } from 'lodash'
import * as M from 'moment'
import { tuple } from './tuple';
const moment: MomentRangeExtends & Moment = extendMoment(M)

type Moment = M.Moment

interface DateRangeDef {
  startDate: Moment,
  endDate: Moment
}

interface FormattedDateRange {
  fromDate: string,
  toDate: string,
  dates: string[]
}

class DateHelper {
  public static shortDateFormat: 'DD/MM/YYYY'
  public static shortDateTimeFormat: 'DD/MM/YYYY HH:mm'
  public static dateFormat: 'YYYY-MM-DD'
  public static dateRangeDisplayFmt: 'DD MMM, YYYY'
  public static dateSingleDisplayFmt: 'DD MMM, YYYY (dddd)'

  public static getDateRangeFromOption (dateRange: DateRangeDef): FormattedDateRange {
    const dateFormat = this.dateFormat
    const fromDate = dateRange.startDate
    const toDate = dateRange.endDate
    const dates: string[] = []
    const range = moment.range(fromDate, toDate)

    Array.from(range.by('days')).forEach(m => {
      dates.push(m.format(dateFormat))
    })

    return {
      fromDate: fromDate.format(dateFormat),
      toDate: toDate.format(dateFormat),
      dates
    }
  }

  public static dayAsLabel (dateStr: string, numOfDays: number) {
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
  }

  public static isInFuture (dateStr: string) {
    return moment(dateStr).isAfter(moment())
  }

  public static getDate (milliseconds: string) {
    if (!isEmpty(milliseconds)) {
      return new Date(parseInt(milliseconds, 10))
    } else {
      return undefined
    }
  }

  public static formatDate (date: Date|Moment, format?: string) {
    return moment(date).format(format)
  }

  public static shortDate (date: Moment) {
    if (date) {
      return this.formatDate(date, this.shortDateFormat)
    } else {
      return undefined
    }
  }

  public static shortDateTime (date: Date|Moment) {
    if (date) {
      return this.formatDate(date, this.shortDateTimeFormat)
    } else {
      return undefined
    }
  }

  /**
   * Calculate days range between startDate and endDate,
   * if more than given days, it will adjust the endDate
   *
   * @days - days different to check. Must be 0 or more.
   * returns {startDate: startDate, endDate: adjustedEndDate}
   */
  public static keepDateInRange (startDate: Moment, endDate: Moment, days: number): DateRangeDef {
    if (days < 0) {
      throw new Error(`days must be more 0 or more: ${days}`)
    }
    const range = moment.range(startDate, endDate)
    const adjustedEndDate = range.diff('days') >= days
      ? moment(startDate).days(startDate.days() + (days - 1))
      : endDate
    return {
      startDate,
      endDate: adjustedEndDate
    }
  }

  public static getDefaultDateRange () {
    return {
      'This week': {
        startDate: function startDate (now: Moment) {
          return now.weekday(0)
        },
        endDate: function endDate (now: Moment) {
          return now.weekday(6)
        }
      },
      'Last week': {
        startDate: function startDate (now: Moment) {
          return now.weekday(-7)
        },
        endDate: function endDate (now: Moment) {
          return now.weekday(-1)
        }
      },
      'This month': {
        startDate: function startDate (now: Moment) {
          return now.date(1)
        },
        endDate: function endDate (now: Moment) {
          return now.month(now.month() + 1).date(0)
        }
      },
      'Last month': {
        startDate: function startDate (now: Moment) {
          return now.month(now.month() - 1).date(1)
        },
        endDate: function endDate (now: Moment) {
          return now.date(0)
        }
      },
      'This year': {
        startDate: function startDate (now: Moment) {
          return now.year(now.year()).month(0).date(1)
        },
        endDate: function endDate (now: Moment) {
          return now.year(now.year()).month(11).date(31)
        }
      }
    }
  }

  public static getDateRange (option: DateRangeName): DateRangeDef {
    const dateRange = this.getDefaultDateRange()[option]
    const now = moment()
    return {
      startDate: dateRange.startDate(now),
      endDate: dateRange.endDate(now)
    }
  }

  public static getDateRangeLabel (dateRange: DateRangeDef) {
    const now = moment()
    return findKey(this.getDefaultDateRange(), function (range) {
      return moment(range.startDate(now)).isSame(dateRange.startDate, 'day') &&
        moment(range.endDate(now)).isSame(dateRange.endDate, 'day')
    })
  }
}

const dateRangeNames = tuple(
  'Last week',
  'This month',
  'Last month',
  'This year'
)
type DateRangeName = typeof dateRangeNames[number]

export default DateHelper
