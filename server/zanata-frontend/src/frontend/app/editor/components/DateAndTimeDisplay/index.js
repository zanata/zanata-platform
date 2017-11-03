import React from 'react'
import PropTypes from 'prop-types'
import { FormattedDate, FormattedTime } from 'react-intl'
import Icon from '../../../components/Icon'

/**
 * Display a date and time with an icon.
 */
const DateAndTimeDisplay = ({
  className,
  dateTime
}) => {
  return (
    <span className={className}>
      <Icon name="clock" className="n1" />&nbsp;
      <FormattedDate value={dateTime}
        format="medium" />&nbsp;
      <FormattedTime value={dateTime} />
    </span>
  )
}

DateAndTimeDisplay.propTypes = {
  className: PropTypes.string,
  dateTime: PropTypes.instanceOf(Date).isRequired
}

export default DateAndTimeDisplay
