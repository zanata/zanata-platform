import React, { PropTypes } from 'react'
import { ContentStates } from '../../constants/Options'
import dateUtil from '../../utils/DateHelper'
import {
  Base
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

/**
 * Clickable date and word count component for daily statistics
 */
const cssClass = {
    total: 'btn-primary',
    approved: 'btn-primary',
    translated: 'btn-success',
    needswork: 'btn-warning'
}

const DayMatrix = ({
  dateLabel,
  date,
  wordCount,
  selectedDay,
  selectedContentState,
  handleSelectedDayChanged,
  ...props
}) => {
  let btnClass = date === selectedDay ? 'Bgc(t) C(#fff)' : ''
  btnClass += selectedContentState ? cssClass[selectedContentState.toLowerCase().replace(' ', '')] : 'btn-primary'
  const dateIsInFuture = date ? dateUtil.isInFuture(date) : false
  /* eslint-disable react/jsx-no-bind */
  return (
    <td>
      {date
        ? <Button bsStyle='primary'
          onClick={() => handleSelectedDayChanged(date)}
          className={btnClass}
          disabled={dateIsInFuture || !date}
          title={wordCount + ' words'}>
          <div className='calDate'>{date ? dateLabel : '\u00a0'}</div>
          <div className='calInfo'>
            {dateIsInFuture ? '\u00a0' : wordCount}
          </div>
        </Button>
        : <Base atomic={{bgc: 'Bgc(#fff.85)', stretchedBox: 'StretchedBox'}} />}
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
