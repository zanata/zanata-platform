// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import DatePicker from 'antd/lib/date-picker'
import 'antd/lib/date-picker/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import utilsDate from '../../utils/DateHelper'
import ContributionChart from './ContributionChart'
import FilterableMatrixTable from './FilterableMatrixTable'

const STATS_MAX_DAYS = 365

/**
 * User profile statistics root page
 */
class RecentContributions extends React.Component {
  static propTypes = {
    matrixForAllDays: PropTypes.array,
    dateRange: PropTypes.object,
    wordCountsForSelectedDayFilteredByContentState: PropTypes.array,
    wordCountsForEachDayFilteredByContentState: PropTypes.array,
    contentStateOption: PropTypes.string,
    selectedDay: PropTypes.string,
    handleDateRangeChanged: PropTypes.func,
    handleFilterChanged: PropTypes.func,
    handleSelectedDayChanged: PropTypes.func.isRequired
  }

  onDateRangeChanged = (dateRange) => {
    const dateRangeFormat = {
      endDate: dateRange[1],
      startDate: dateRange[0]
    }
    // adjust dateRange to be in STATS_MAX_DAYS
    const adjustedDateRange =
      utilsDate.keepDateInRange(
        dateRangeFormat.startDate, dateRangeFormat.endDate, STATS_MAX_DAYS)
    this.props.handleDateRangeChanged(adjustedDateRange)
  }

  render () {
    const { RangePicker } = DatePicker
    const {
      dateRange,
      matrixForAllDays,
      wordCountsForSelectedDayFilteredByContentState,
      wordCountsForEachDayFilteredByContentState,
      contentStateOption,
      selectedDay,
      handleFilterChanged,
      handleSelectedDayChanged
    } = this.props
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='matrixHeading' id='userProfile-matrix'>
        <Row>
          <h2 className='userProfile-recentContributions'>
          Recent Contributions</h2>
          <div className='fr'>
            <RangePicker
              defaultValue={[dateRange.startDate, dateRange.endDate]}
              onChange={this.onDateRangeChanged}
              dateRender={(current) => {
                const style = {}
                if (current.date() === 1) {
                  style.border = '1px solid #03A6D7'
                  style.borderRadius = '50%'
                }
                return (
                  <div className="ant-calendar-date" style={style}>
                    {current.date()}
                  </div>
                )
              }}
            />
          </div>
        </Row>
        <Row>
          <div className='flexChart-container'>
            <ContributionChart
              wordCountForEachDay={matrixForAllDays}
              dateRange={dateRange} />
          </div>
        </Row>
        <Row>
          <FilterableMatrixTable
            wordCountForSelectedDay={wordCountsForSelectedDayFilteredByContentState} // eslint-disable-line max-len
            wordCountForEachDay={wordCountsForEachDayFilteredByContentState}
            dateRange={dateRange}
            selectedContentState={contentStateOption}
            selectedDay={selectedDay}
            handleFilterChanged={handleFilterChanged}
            handleSelectedDayChanged={handleSelectedDayChanged}
          />
        </Row>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default RecentContributions
