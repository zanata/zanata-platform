import React, { PropTypes } from 'react'
import moment from 'moment'
import { merge, range } from 'lodash'
import DayMatrix from './DayMatrix'
import { ContentStates } from '../../constants/Options'
import {
  Base,
  ButtonLink,
  Flex
} from 'zanata-ui'

const classes = {
  calendar: {
    base: {
      m: 'Mb(r1)',
      tbl: 'Tbl(f)',
      w: 'W(100%)'
    },
    types: {
      total: {
        c: ''
      },
      approved: {
        c: 'C(pri)'
      },
      translated: {
        c: 'C(success)'
      },
      needswork: {
        c: 'C(unsure)'
      }
    }
  }
}
const CalendarMonthMatrix = ({
  matrixData,
  selectedDay,
  selectedContentState,
  dateRangeOption,
  handleSelectedDayChanged,
  ...props
}) => {
  if (matrixData.length === 0) {
    return <table><tbody><tr><td>Loading</td></tr></tbody></table>
  }

  const calTheme = merge({},
    classes.calendar.base,
    classes.calendar
      .types[selectedContentState.toLowerCase().replace(' ', '')]
  )

  let days = []
  let result = []

  const firstDay = moment(matrixData[0]['date'])
  for (var i = firstDay.weekday() - 1; i >= 0; i--) {
    // for the first week, we pre-fill missing week days
    days.push(
      <DayMatrix key={firstDay.weekday(i).format()} />
    )
  }

  matrixData.forEach((entry) => {
    const date = entry['date']
    days.push(
      <DayMatrix key={date}
        dateLabel={moment(date).format('Do')}
        date={date}
        wordCount={entry['wordCount']}
        selectedDay={selectedDay}
        handleSelectedDayChanged={handleSelectedDayChanged} />
    )
  })

  while (days.length) {
    const dayColumns = days.splice(0, 7)
    result.push(
      <tr
        key={dateRangeOption.value + '-week' + result.length}>
        {dayColumns}
      </tr>
    )
  }

  // this is to make week days locale aware and making sure it align with
  // below display
  const now = moment()
  const weekDays = range(0, 7).map(i => {
    const weekDay = now.weekday(i).format('ddd')
    return <th key={weekDay}>{weekDay}</th>
  })

  /* eslint-disable react/jsx-no-bind */
  return (
    <div>
      <Flex atomic={{m: 'Mb(rh)'}}>
        <div>
          <h3 className='Fw(600) Tt(u)'>
            {dateRangeOption.label}'s Activity
          </h3>
        </div>
        {selectedDay &&
        (<div className='Mstart(a)'>
          <ButtonLink
            onClick={() => handleSelectedDayChanged(null)}>
            Clear selection
          </ButtonLink>
        </div>)}
      </Flex>
      <Base tagName='table' theme={calTheme}>
        <thead>
          <tr>{weekDays}</tr>
        </thead>
        <tbody>{result}</tbody>
      </Base>
    </div>
  )
  /* eslint-enable react/jsx-no-bind */
}

CalendarMonthMatrix.propTypes = {
  matrixData: PropTypes.arrayOf(
    PropTypes.shape({
      date: PropTypes.string.isRequired,
      wordCount: PropTypes.number.isRequired
    })
  ).isRequired,
  selectedDay: PropTypes.string,
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  dateRangeOption: PropTypes.object.isRequired,
  handleSelectedDayChanged: PropTypes.func
}

export default CalendarMonthMatrix
