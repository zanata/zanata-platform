import moment from 'moment-range';

var DateHelper = {
  dateFormat: 'YYYY-MM-DD',
  getDateRangeFromOption: function (dateRangeOption) {
    var now = moment(),
      dateFormat = this.dateFormat,
      dates = [],
      range,
      fromDate,
      toDate;

    switch(dateRangeOption) {
      case 'This Week':
        fromDate = moment().weekday(0);
        toDate = moment().weekday(6);
        break;
      case 'Last Week':
        fromDate = moment().weekday(-7);
        toDate = moment().weekday(-1);
        break;
      case 'This Month':
        fromDate = moment().date(1);
        toDate = moment().month(now.month() + 1).date(0);
        break;
      case 'Last Month':
        fromDate = moment().month(now.month() - 1).date(1);
        toDate = moment().date(0);
        break;
      default:
        console.error('selectedDateRange [%s] can not be matched. Using (This Week) instead.', dateRangeOption);
        fromDate = moment().weekday(0);
        toDate = moment();
    }

    range = moment().range(fromDate, toDate);

    range.by('days', function(moment) {
      dates.push(moment.format(dateFormat));
    });

    return {
      fromDate: fromDate.format(dateFormat),
      toDate: toDate.format(dateFormat),
      dates: dates
    }
  },

  dayAsLabel: function(dateStr, numOfDays, useFullName) {
    var date = moment(dateStr),
      dayOfWeekFmt,
      dayOfMonthFmt;

    dayOfWeekFmt = useFullName ? 'dddd (Do MMM)' : 'ddd';
    dayOfMonthFmt = useFullName ? 'Do MMM (dddd)' : 'D';
    if (numOfDays < 8) {
      return date.format(dayOfWeekFmt);
    } else {
      return date.format(dayOfMonthFmt);
    }
  },

  isInFuture: function(dateStr) {
    return moment(dateStr).isAfter(moment());
  }
};

export default DateHelper;
