import React from 'react'
import PropTypes from 'prop-types'
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

  constructor (props) {
    super(props)
    this.state = {
      dateRange: props.dateRange,
      showDateRange: false
    }
  }

  onToggleShowDateRange = () => {
    this.setState(prevState => ({
      showDateRange: !prevState.showDateRange
    }))
  }

  onDateRangeChanged = (dateRange) => {
    // adjust dateRange to be in STATS_MAX_DAYS
    const adjustedDateRange =
      utilsDate.keepDateInRange(
        dateRange.startDate, dateRange.endDate, STATS_MAX_DAYS)
    this.setState({
      dateRange: adjustedDateRange
    })
  }

  render () {
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
      ' to ' + utilsDate.shortDate(this.state.dateRange.endDate)

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
                    <Button bsStyle='primary'
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
}

export default RecentContributions
