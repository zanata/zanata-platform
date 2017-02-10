import React, { PropTypes } from 'react'
import dateUtils from '../../utils/DateHelper'

const CalendarPeriodHeading = ({
  selectedDay,
  dateRange,
  ...props
}) => {
  const period = selectedDay
    ? dateUtils.formatDate(selectedDay, dateUtils.dateSingleDisplayFmt)
    : dateUtils.formatDate(dateRange.startDate, dateUtils.dateRangeDisplayFmt) +
      ' … ' +
      dateUtils.formatDate(dateRange.endDate, dateUtils.dateRangeDisplayFmt)

  return (
    <div className='calendar-activity'>
      <h3 className='text-bold-uppercase'>Activity Details</h3>
      <p className='text-muted'>{period}</p>
    </div>
  )
}

CalendarPeriodHeading.propTypes = {
  selectedDay: PropTypes.string,
  dateRange: PropTypes.object
}

export default CalendarPeriodHeading
