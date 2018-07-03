// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import ContributionChart from './ContributionChart'
import FilterableMatrixTable from './FilterableMatrixTable'
import { DateRange } from 'react-date-range'
import utilsDate from '../../utils/DateHelper'
import { TextInput } from '../../components'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'

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
      <div className='matrixHeading' id='userProfile-matrix'>
        <div className='u-flexCenter'>
          <h2 className='userProfile-recentContributions'>
          Recent Contributions</h2>
          <div className='dateRange-container'>
            <Button className='btn-link fr txt-primary pl1 pr1' aria-label='button'
              onClick={() => this.onToggleShowDateRange()}>
              <span className='dateRange-textField'>
                <TextInput editable={false} value={displayDateRange} />
              </span>
            </Button>

            {this.state.showDateRange &&
              <Modal
                id='profile'
                title={'Date range selection'}
                width={'54rem'}
                visible={this.state.showDateRange}
                onCancel={() => this.onToggleShowDateRange()}
                footer={[
                  <Button key='back'
                    aria-label='button'
                    onClick={() => this.onToggleShowDateRange()}>
                    Cancel
                  </Button>,
                  <Button key='ok' type='primary'
                    aria-label='button'
                    onClick={
                    () => handleDateRangeChanged(this.state.dateRange)}>
                    Apply
                  </Button>]} >
                <span className='txt-muted'>(Maximum 365 days)</span>
                <DateRange
                  startDate={this.state.dateRange.startDate}
                  endDate={this.state.dateRange.endDate}
                  ranges={utilsDate.getDefaultDateRange()}
                  className='dateRange-calendar'
                  onChange={this.onDateRangeChanged} />
              </Modal>
            }
          </div>
        </div>
        <div className='flexChart-container'>
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
