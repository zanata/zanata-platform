import React, { PropTypes } from 'react'
import ContributionChart from './ContributionChart'
import FilterableMatrixTable from './FilterableMatrixTable'
import { DateRanges } from '../../constants/Options'
import {
  Base,
  Flex,
  Select
} from 'zanata-ui'

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
  dropDownContainer: {
    m: 'Mstart(a)',
    miw: 'Miw(r6)'
  },
  chartContainer: {
    m: 'Mb(r1)',
    mih: 'Mih(r4)'
  }
}
/**
 * User profile statistics root page
 */
const RecentContributions = ({
  dateRange,
  matrixForAllDays,
  dateRangeOption,
  wordCountsForSelectedDayFilteredByContentState,
  wordCountsForEachDayFilteredByContentState,
  contentStateOption,
  selectedDay,
  handleDateRangeChanged,
  handleFilterChanged,
  handleSelectedDayChanged
}) => {
  return (
    <Base atomic={classes.root} id='profile-matrix'>
      <Flex align='c'>
        <Base tagName='h2' atomic={classes.heading}>Recent Contributions</Base>
        <Base atomic={classes.dropDownContainer}>
          <Select
            name='dateRange'
            className='Flx(flx1)'
            searchable={false}
            clearable={false}
            value={dateRangeOption}
            options={DateRanges}
            onChange={handleDateRangeChanged} />
        </Base>
      </Flex>
      <Flex dir='c' align='c' justify='c' atomic={classes.chartContainer}>
        <ContributionChart
          wordCountForEachDay={matrixForAllDays}
          dateRangeOption={dateRangeOption} />
      </Flex>
      <FilterableMatrixTable
        wordCountForSelectedDay={wordCountsForSelectedDayFilteredByContentState}
        wordCountForEachDay={wordCountsForEachDayFilteredByContentState}
        fromDate={dateRange.fromDate} toDate={dateRange.toDate}
        dateRangeOption={dateRangeOption}
        selectedContentState={contentStateOption}
        selectedDay={selectedDay}
        handleFilterChanged={handleFilterChanged}
        handleSelectedDayChanged={handleSelectedDayChanged}
      />
    </Base>
  )
}

RecentContributions.propTypes = {
  dateRange: PropTypes.object,
  matrixForAllDays: PropTypes.array,
  dateRangeOption: PropTypes.object,
  wordCountsForSelectedDayFilteredByContentState: PropTypes.array,
  wordCountsForEachDayFilteredByContentState: PropTypes.array,
  contentStateOption: PropTypes.string,
  selectedDay: PropTypes.string,
  handleDateRangeChanged: PropTypes.func,
  handleFilterChanged: PropTypes.func,
  handleSelectedDayChanged: PropTypes.func
}

export default RecentContributions
