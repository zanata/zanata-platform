// @ts-check
import * as PropTypes from 'prop-types'
import React from 'react'
import dateUtils from '../../utils/DateHelper'

/** @type { React.StatelessComponent<{selectedDay, dateRange, props?}> } */
const CalendarPeriodHeading = ({
  selectedDay,
  dateRange,
  // @ts-ignore: unused?
  ...props
}) => {
  const period = selectedDay
    ? dateUtils.formatDate(selectedDay, dateUtils.dateSingleDisplayFmt)
    : dateUtils.formatDate(dateRange.startDate, dateUtils.dateRangeDisplayFmt) +
      ' to ' +
      dateUtils.formatDate(dateRange.endDate, dateUtils.dateRangeDisplayFmt)

  return (
    <div className='userProfile-calendarActivity'>
      <h3 className='u-textUppercaseBold'>Activity Details</h3>
      <p className='u-textMuted'>{period}</p>
    </div>
  )
}

CalendarPeriodHeading.propTypes = {
  selectedDay: PropTypes.string,
  dateRange: PropTypes.object
}

export default CalendarPeriodHeading
