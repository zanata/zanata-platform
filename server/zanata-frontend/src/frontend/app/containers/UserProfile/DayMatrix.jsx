import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import dateUtil from '../../utils/DateHelper'
import {
  Base,
  Button
} from 'zanata-ui'

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
  calButton: {
    base: {
      d: 'D(b)',
      bgc: 'Bgc(#fff.7)',
      h: 'H(100%)',
      w: 'W(100%)',
      disabled: {
        op: '',
        bgc: 'Bgc(#fff.85):di'
      }
    },
    active: {
      bgc: 'Bgc(t)',
      c: 'C(#fff)'
    }
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
  const buttonTheme = {
    base: merge({},
      classes.calButton.base,
      date === selectedDay && classes.calButton.active
    )
  }
  /* eslint-disable react/jsx-no-bind */
  return (
    <Base tagName='td' theme={classes.root}>
      {date
        ? <Button onClick={() => handleSelectedDayChanged(date)}
          disabled={dateIsInFuture || !date}
          theme={buttonTheme}
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
