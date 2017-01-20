import React, { PropTypes } from 'react'
import moment from 'moment'
import { range } from 'lodash'
import DayMatrix from './DayMatrix'
import { ContentStates } from '../../constants/Options'
import utilsDate from '../../utils/DateHelper'
import { Button } from 'react-bootstrap'

const classes = {
  calendar: {
    types: {
      total: '',
      approved: 'C(pri)',
      translated: 'C(success)',
      needswork: 'C(unsure)'
    }
  }
}
const CalendarMonthMatrix = ({
  matrixData,
  selectedDay,
  selectedContentState,
  dateRange,
  handleSelectedDayChanged,
  ...props
}) => {
  if (matrixData.length === 0) {
    return <table><tbody><tr><td>Loading</td></tr></tbody></table>
  }

  const calClass = 'activity-graph Mb(r1) Tbl(f) W(100%) ' + classes.calendar
      .types[selectedContentState.toLowerCase().replace(' ', '')]

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
        selectedContentState={selectedContentState}
        dateLabel={moment(date).format('Do')}
        date={date}
        wordCount={entry['wordCount']}
        selectedDay={selectedDay}
        handleSelectedDayChanged={handleSelectedDayChanged} />
    )
  })

  while (days.length) {
    const dayColumns = days.splice(0, 7)
    const key = utilsDate.shortDate(dateRange.startDate) + '-' +
      utilsDate.shortDate(dateRange.endDate) + '-week' + result.length
    result.push(
      <tr key={key}>
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

  let header = utilsDate.getDateRangeLabel(dateRange)
  header = header ? header + '\'s Activity'
    : utilsDate.formatDate(dateRange.startDate, utilsDate.dateRangeDisplayFmt) +
      ' … ' +
      utilsDate.formatDate(dateRange.endDate, utilsDate.dateRangeDisplayFmt)
  /* eslint-disable react/jsx-no-bind */
  return (
    <div id='activity-table'>
      <div className='activity-table-inner'>
        <div>
          <h3 className='Fw(600) Tt(u)'>
            {header}
          </h3>
        </div>
        {selectedDay &&
        (<div>
          <Button bsStyle='link'
            className='btn-clear'
            onClick={() => handleSelectedDayChanged(null)}>
            Clear selection
          </Button>
        </div>)}
      </div>
      <table className={calClass}>
        <thead>
          <tr>{weekDays}</tr>
        </thead>
        <tbody>{result}</tbody>
      </table>
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
  dateRange: PropTypes.object.isRequired,
  handleSelectedDayChanged: PropTypes.func.isRequired
}

export default CalendarMonthMatrix
