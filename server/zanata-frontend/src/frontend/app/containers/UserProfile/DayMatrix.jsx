import React, { PropTypes } from 'react'
import dateUtil from '../../utils/DateHelper'
import {
  Base
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

/**
 * Clickable date and word count component for daily statistics
 */

const DayMatrix = ({
  dateLabel,
  date,
  wordCount,
  selectedDay,
  handleSelectedDayChanged,
  ...props
}) => {
  const dateIsInFuture = date ? dateUtil.isInFuture(date) : false
  /* eslint-disable react/jsx-no-bind */
  return (
    <Base tagName='td' className='activity-graph'>
      {date
        ? <Button bsStyle='primary'
          onClick={() => handleSelectedDayChanged(date)}
          disabled={dateIsInFuture || !date}
          title={wordCount + ' words'}>
          <Base className='calDate'>{date ? dateLabel : '\u00a0'}</Base>
          <Base className='calInfo'>
            {dateIsInFuture ? '\u00a0' : wordCount}
          </Base>
        </Button>
        : <Base atomic={{bgc: 'Bgc(#fff.85)', stretchedBox: 'StretchedBox'}} />}
    </Base>
  )
  /* eslint-enable react/jsx-no-bind */
}

DayMatrix.propTypes = {
  dateLabel: PropTypes.string,
  date: PropTypes.string,
  wordCount: PropTypes.number,
  selectedDay: PropTypes.string,
  handleSelectedDayChanged: PropTypes.func.isRequired
}

export default DayMatrix
