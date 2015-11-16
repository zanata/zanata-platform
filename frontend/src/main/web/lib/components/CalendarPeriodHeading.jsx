import React from 'react';
import moment from 'moment';
import dateUtils from '../utils/DateHelper'

var CalendarPeriodHeading = React.createClass(
  {
    render: function() {
      var stdFmt = dateUtils['dateFormat'],
        dateDisplayFmt = 'DD MMM, YYYY (dddd)',
        dateRangeDisplayFmt = 'DD MMM, YYYY',
        period;

      if (this.props.selectedDay) {
        period = moment(this.props.selectedDay, stdFmt).format(dateDisplayFmt);
      } else {
        period = moment(this.props.fromDate, stdFmt).format(dateRangeDisplayFmt)
        + ' … '
        + moment(this.props.toDate, stdFmt).format(dateRangeDisplayFmt)
        + ' (' + this.props.dateRange + ')';
      }
      return (
        <div className='l--push-bottom-half'>
          <h3 className='epsilon txt--uppercase'>Activity Details</h3>
          <p className="txt--understated">{period}</p>
        </div>
      )
    }
  }
);

export default CalendarPeriodHeading;
