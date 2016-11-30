import React, { PropTypes } from 'react'
import ContributionChart from './ContributionChart'
import FilterableMatrixTable from './FilterableMatrixTable'
import { DateRange } from 'react-date-range'
import utilsDate from '../../utils/DateHelper'
import {
  Base,
  Flex,
  Modal
} from 'zanata-ui'

import TextInput from '../../components'
import { Button, ButtonGroup } from 'react-bootstrap'

const classes = {
  root: {
    flxg: 'Flxg(1)',
    flxs: 'Flxg(0)',
    m: 'Mstart(r2)--md',
    maw: 'Maw(100%)',
    miw: 'Miw(100%) Miw(0)--md'
  },
  heading: {
    fz: 'Fz(ms1)',
    fw: 'Fw(600)',
    tt: 'Tt(u)'
  },
  dateRangeContainer: {
    m: 'Mstart(a)',
    miw: 'Miw(r6)'
  },
  chartContainer: {
    m: 'Mb(r1)',
    mih: 'Mih(r4)'
  },
  calendar: {
    DateRange: {
      width: '100%'
    }
  },
  dataRangeTextField: {
    base: {
      w: 'W(r8)'
    }
  }
}

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
      <Base atomic={classes.root} id='profile-matrix'>
        <Flex align='c'>
          <Base tagName='h2' atomic={classes.heading}>
            Recent Contributions
          </Base>
          <Base atomic={classes.dateRangeContainer}>
            <Button bsStyle='link' onClick={() => this.onToggleShowDateRange()}>
              <TextInput editable={false} value={displayDateRange}
                theme={classes.dataRangeTextField} />
            </Button>

            {this.state.showDateRange &&
              <Modal show={this.state.showDateRange}
                onHide={() => this.onToggleShowDateRange()}>
                <Modal.Header>
                  <Modal.Title>Date range selection</Modal.Title>
                  <span className='C(muted)'>(Maximum 365 days)</span>
                </Modal.Header>
                <Modal.Body>
                  <DateRange
                    startDate={this.state.dateRange.startDate}
                    endDate={this.state.dateRange.endDate}
                    ranges={utilsDate.getDefaultDateRange()}
                    theme={classes.calendar}
                    onChange={this.onDateRangeChanged} />
                </Modal.Body>
                <Modal.Footer>
                  <ButtonGroup className='pull-right'>
                    <Button bsStyle='link' onClick={() =>
                      this.onToggleShowDateRange()}>
                      Cancel
                    </Button>
                    <Button bsStyle='default' atomic={{m: 'Mstart(r1)'}}
                      onClick={
                      () => handleDateRangeChanged(this.state.dateRange)}>
                      Apply
                    </Button>
                  </ButtonGroup>
                </Modal.Footer>
              </Modal>
            }
          </Base>
        </Flex>
        <Flex dir='c' align='c' justify='c' atomic={classes.chartContainer}>
          <ContributionChart
            wordCountForEachDay={matrixForAllDays}
            dateRange={dateRange} />
        </Flex>
        <FilterableMatrixTable
          wordCountForSelectedDay={wordCountsForSelectedDayFilteredByContentState} // eslint-disable-line max-len
          wordCountForEachDay={wordCountsForEachDayFilteredByContentState}
          dateRange={dateRange}
          selectedContentState={contentStateOption}
          selectedDay={selectedDay}
          handleFilterChanged={handleFilterChanged}
          handleSelectedDayChanged={handleSelectedDayChanged}
        />
      </Base>
    )
    /* eslint-enable react/jsx-no-bind */
  }
})

export default RecentContributions
