import React, { PropTypes } from 'react'
import dateUtil from '../../utils/DateHelper'
import {
  Base
} from 'zanata-ui'
import { Button } from 'react-bootstrap'

const classes = {
  root: {
    bd: 'Bdw(rq) Bdc(t) Bds(s)',
    bgc: 'Bgc(cc)',
    bgcp: 'Bgcp(pb)',
    ov: 'Ov(h)',
    p: 'P(0)',
    pos: 'Pos(r)',
    ta: 'Ta(c)',
    va: 'Va(t)'
  },
  calDate: {
    fz: 'Fz(msn1)',
    p: 'Py(re)'
  },
  calInfo: {
    bgc: 'Bgc(#fff.4)',
    fw: 'Fw(600)',
    p: 'Py(re)'
  }
}
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
    <Base tagName='td' theme={classes.root}>
      {date
        ? <Button bsStyle='primary'
          onClick={() => handleSelectedDayChanged(date)}
          disabled={dateIsInFuture || !date}
          title={wordCount + ' words'}>
          <Base atomic={classes.calDate}>{date ? dateLabel : '\u00a0'}</Base>
          <Base atomic={classes.calInfo}>
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
