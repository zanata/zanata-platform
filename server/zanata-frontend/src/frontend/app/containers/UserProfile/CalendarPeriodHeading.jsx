import React, { PropTypes } from 'react'
import moment from 'moment'
import dateUtils from '../../utils/DateHelper'

const CalendarPeriodHeading = ({
  selectedDay,
  fromDate,
  toDate,
  dateRange,
  ...props
}) => {
  const stdFmt = dateUtils['dateFormat']
  const dateDisplayFmt = 'DD MMM, YYYY (dddd)'
  const dateRangeDisplayFmt = 'DD MMM, YYYY'

  const period = selectedDay
    ? moment(selectedDay, stdFmt).format(dateDisplayFmt)
    : moment(fromDate, stdFmt).format(dateRangeDisplayFmt) +
      ' … ' +
      moment(toDate, stdFmt).format(dateRangeDisplayFmt) +
      ' (' + dateRange + ')'

  return (
    <div className='Mb(rh)'>
      <h3 className='Fw(600) Tt(u)'>Activity Details</h3>
      <p className='C(muted)'>{period}</p>
    </div>
  )
}

CalendarPeriodHeading.propTypes = {
  selectedDay: PropTypes.string,
  fromDate: PropTypes.string,
  toDate: PropTypes.string,
  dateRange: PropTypes.string
}

export default CalendarPeriodHeading
