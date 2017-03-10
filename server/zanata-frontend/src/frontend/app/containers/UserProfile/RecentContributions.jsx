import React, { PropTypes } from 'react'
import ContributionChart from './ContributionChart'
import FilterableMatrixTable from './FilterableMatrixTable'
import { DateRange } from 'react-date-range'
import utilsDate from '../../utils/DateHelper'
import { Button } from 'react-bootstrap'
import { Modal, TextInput } from '../../components'

const STATS_MAX_DAYS = 365

/**
 * User profile statistics root page
 */
var RecentContributions = React.createClass({
  propTypes: {
    matrixForAllDays: PropTypes.array,
    dateRange: PropTypes.object,
    wordCountsForSelectedDayFilteredByContentState: PropTypes.array,
    wordCountsForEachDayFilteredByContentState: PropTypes.array,
    contentStateOption: PropTypes.string,
    selectedDay: PropTypes.string,
    handleDateRangeChanged: PropTypes.func,
    handleFilterChanged: PropTypes.func,
    handleSelectedDayChanged: PropTypes.func
  },

  getInitialState: function () {
    return {
      dateRange: this.props.dateRange,
      showDateRange: false
    }
  },

  onToggleShowDateRange: function () {
    this.setState({
      showDateRange: !this.state.showDateRange
    })
  },

  onDateRangeChanged: function (dateRange) {
    // adjust dateRange to be in STATS_MAX_DAYS
    const adjustedDateRange =
      utilsDate.keepDateInRange(
        dateRange.startDate, dateRange.endDate, STATS_MAX_DAYS)
    this.setState({
      dateRange: adjustedDateRange
    })
  },

  render: function () {
    const {
      dateRange,
      matrixForAllDays,
      wordCountsForSelectedDayFilteredByContentState,
      wordCountsForEachDayFilteredByContentState,
      contentStateOption,
      selectedDay,
      handleFilterChanged,
      handleSelectedDayChanged,
      handleDateRangeChanged
    } = this.props

    const displayDateRange =
      utilsDate.shortDate(this.state.dateRange.startDate) +
      ' ... ' + utilsDate.shortDate(this.state.dateRange.endDate)

    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='matrix-heading' id='profile-matrix'>
        <div className='flex-center'>
          <h2 className='recent-contrib'>Recent Contributions</h2>
          <div className='daterange-container'>
            <Button bsStyle='link' className='pull-right'
              onClick={() => this.onToggleShowDateRange()}>
              <span className='daterange-textfield'>
                <TextInput editable={false} value={displayDateRange} />
              </span>
            </Button>

            {this.state.showDateRange &&
              <Modal show={this.state.showDateRange}
                onHide={() => this.onToggleShowDateRange()}>
                <Modal.Header>
                  <Modal.Title>Date range selection</Modal.Title>
                  <span className='text-muted'>(Maximum 365 days)</span>
                </Modal.Header>
                <Modal.Body>
                  <DateRange
                    startDate={this.state.dateRange.startDate}
                    endDate={this.state.dateRange.endDate}
                    ranges={utilsDate.getDefaultDateRange()}
                    className='calendar-daterange'
                    onChange={this.onDateRangeChanged} />
                </Modal.Body>
                <Modal.Footer>
                  <span className='pull-right'>
                    <Button bsStyle='link'
                      onClick={() => this.onToggleShowDateRange()}>
                      Cancel
                    </Button>
                    <Button bStyle='primary'
                      onClick={
                      () => handleDateRangeChanged(this.state.dateRange)}>
                      Apply
                    </Button>
                  </span>
                </Modal.Footer>
              </Modal>
            }
          </div>
        </div>
        <div className='flex-chart-container'>
          <ContributionChart
            wordCountForEachDay={matrixForAllDays}
            dateRange={dateRange} />
        </div>
        <FilterableMatrixTable
          wordCountForSelectedDay={wordCountsForSelectedDayFilteredByContentState} // eslint-disable-line max-len
          wordCountForEachDay={wordCountsForEachDayFilteredByContentState}
          dateRange={dateRange}
          selectedContentState={contentStateOption}
          selectedDay={selectedDay}
          handleFilterChanged={handleFilterChanged}
          handleSelectedDayChanged={handleSelectedDayChanged}
        />
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
})

export default RecentContributions
