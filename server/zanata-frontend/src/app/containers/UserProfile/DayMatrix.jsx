import * as PropTypes from 'prop-types'
import React from 'react'
import { ContentStates } from '../../constants/Options'
import dateUtil from '../../utils/DateHelper'
import { Button } from 'react-bootstrap'

/**
 * Clickable date and word count component for daily statistics
 */
const cssClass = {
  total: 'primary',
  approved: 'info',
  translated: 'success',
  needswork: 'warning'
}

/** @type
    { React.StatelessComponent<{key?, dateLabel?, date?, wordCount?,
      selectedDay?, selectedContentState?, handleSelectedDayChanged?}>
    } */
const DayMatrix = ({
  dateLabel,
  date,
  wordCount,
  selectedDay,
  selectedContentState,
  handleSelectedDayChanged,
  // @ts-ignore: unused?
  ...props
}) => {
  const dateIsInFuture = date ? dateUtil.isInFuture(date) : false
  const btnStyle = selectedContentState
    ? cssClass[selectedContentState.toLowerCase().replace(' ', '')]
    : cssClass['total']

  /* eslint-disable react/jsx-no-bind */
  return (
    <td>
      {date
        ? <Button
          bsStyle={btnStyle}
          onClick={() => handleSelectedDayChanged(date)}
          className={date === selectedDay ? 'active ' : ''}
          disabled={dateIsInFuture || !date}
          title={wordCount + ' words'}>
          <div className='cal-date'>{date ? dateLabel : '\u00a0'}</div>
          <div className='cal-info'>
            {dateIsInFuture ? '\u00a0' : wordCount}
          </div>
        </Button>
        : <div className='matrixBox'></div>}
    </td>
  )
  /* eslint-enable react/jsx-no-bind */
}

DayMatrix.propTypes = {
  dateLabel: PropTypes.string,
  date: PropTypes.string,
  wordCount: PropTypes.number,
  selectedDay: PropTypes.string,
  selectedContentState: PropTypes.oneOf(ContentStates),
  handleSelectedDayChanged: PropTypes.func.isRequired
}

export default DayMatrix
