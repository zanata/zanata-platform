import React from 'react'
import moment from 'moment'
import { merge, range, map } from 'lodash'
import DayMatrix from './DayMatrix'
import Actions from '../../actions/userMatrix'
import { ContentStates } from '../../constants/Options'
import {
  Base,
  ButtonLink,
  Flex
} from '../../components'

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

var CalendarMonthMatrix = React.createClass({
  propTypes: {
    matrixData: React.PropTypes.arrayOf(
      React.PropTypes.shape(
        {
          date: React.PropTypes.string.isRequired,
          wordCount: React.PropTypes.number.isRequired
        })
    ).isRequired,
    selectedDay: React.PropTypes.string,
    selectedContentState: React.PropTypes.oneOf(ContentStates).isRequired,
    dateRangeOption: React.PropTypes.object.isRequired
  },

  getDefaultProps: function () {
    // this is to make week days locale aware and making sure it align with
    // below display
    var now = moment()
    var weekDay
    var weekDays = range(0, 7).map(i => {
      weekDay = now.weekday(i).format('ddd')
      return <th key={weekDay}>{weekDay}</th>
    })

    return {
      weekDays: weekDays
    }
  },
  handleClearSelection: function () {
    Actions.clearSelectedDay()
  },
  render: function () {
    const {
      selectedDay,
      selectedContentState,
      matrixData,
      weekDays
    } = this.props
    const calTheme = merge({},
      classes.calendar.base,
      classes.calendar
        .types[selectedContentState.toLowerCase().replace(' ', '')]
    )

    let days = []
    let result = []
    let dayColumns
    let firstDay
    let heading

    if (matrixData.length === 0) {
      return <table><tbody><tr><td>Loading</td></tr></tbody></table>
    }

    firstDay = moment(matrixData[0]['date'])
    for (var i = firstDay.weekday() - 1; i >= 0; i--) {
      // for the first week, we pre-fill missing week days
      days.push(
        <DayMatrix key={firstDay.weekday(i).format()} />
      )
    }

    matrixData.forEach(function (entry) {
      var date = entry['date']
      days.push(
        <DayMatrix key={date}
          dateLabel={moment(date).format('Do')}
          date={date}
          wordCount={entry['wordCount']}
          selectedDay={selectedDay} />
      )
    })

    while (days.length) {
      dayColumns = days.splice(0, 7)
      result.push(
        <tr
          key={this.props.dateRangeOption.value + '-week' + result.length}>
          {dayColumns}
        </tr>
      )
    }

    heading = (
      <Flex atomic={{m: 'Mb(rh)'}}>
        <div>
          <h3 className='Fw(600) Tt(u)'>
            {this.props.dateRangeOption.label}'s Activity
          </h3>
        </div>
        {selectedDay &&
          (<div className='Mstart(a)'>
            <ButtonLink
              onClick={this.handleClearSelection}>
              Clear selection
            </ButtonLink>
          </div>)}
      </Flex>
    )

    return (
      <div>
        {heading}
        <Base tagName='table' theme={calTheme}>
          <thead>
            <tr>{weekDays}</tr>
          </thead>
          <tbody>
            {result}
          </tbody>
        </Base>
      </div>
    )
  }
})

export default CalendarMonthMatrix
