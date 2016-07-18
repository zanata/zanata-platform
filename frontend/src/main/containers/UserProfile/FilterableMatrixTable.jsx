import React, { PropTypes } from 'react'
import ContentStateFilter from './ContentStateFilter'
import CalendarMonthMatrix from './CalendarMonthMatrix'
import CalendarPeriodHeading from './CalendarPeriodHeading'
import CategoryMatrixTable from './CategoryMatrixTable'
import {
  Base,
  Flex
} from 'zanata-ui'
import {
  DateRanges,
  ContentStates
} from '../../constants/Options'
/**
 * Bottom section of contribution statistic page.
 * See RecentContribution for main page.
 */
const FilterableMatrixTable = ({
  dateRangeOption,
  fromDate,
  selectedContentState,
  selectedDay,
  toDate,
  wordCountForEachDay,
  wordCountForSelectedDay,
  handleFilterChanged,
  handleSelectedDayChanged
}) => {
  const categoryTables = (wordCountForSelectedDay.length > 0)
    ? ([
      <CategoryMatrixTable
        key='locales'
        matrixData={wordCountForSelectedDay}
        category='localeId'
        categoryTitle='localeDisplayName'
        categoryName='Languages' />,
      <CategoryMatrixTable
        key='projects'
        matrixData={wordCountForSelectedDay}
        category='projectSlug'
        categoryTitle='projectName'
        categoryName='Projects' />
    ])
    : <Base atomic={{c: 'C(muted)'}}>No contributions</Base>
  return (
    <div>
      <ContentStateFilter selectedContentState={selectedContentState}
        handleFilterChanged={handleFilterChanged} />
      <Flex atomic={{fld: 'Fld(c) Fld(r)--lg'}}>
        <div className='W(100%) W(1/2)--lg Mend(rh)--lg'>
          <CalendarMonthMatrix
            matrixData={wordCountForEachDay}
            selectedDay={selectedDay}
            selectedContentState={selectedContentState}
            dateRangeOption={dateRangeOption}
            handleSelectedDayChanged={handleSelectedDayChanged} />
        </div>
        <div className='W(100%) W(1/2)--lg Mstart(rh)--lg'>
          <CalendarPeriodHeading
            fromDate={fromDate}
            toDate={toDate}
            dateRange={dateRangeOption.label}
            selectedDay={selectedDay} />
          {categoryTables}
        </div>
      </Flex>
    </div>
  )
}

FilterableMatrixTable.propTypes = {
  wordCountForEachDay: PropTypes.arrayOf(
    PropTypes.shape(
      {
        date: PropTypes.string.isRequired,
        wordCount: PropTypes.number.isRequired
      })
  ).isRequired,
  wordCountForSelectedDay: PropTypes.arrayOf(
    PropTypes.shape(
      {
        savedDate: PropTypes.string.isRequired,
        projectSlug: PropTypes.string.isRequired,
        projectName: PropTypes.string.isRequired,
        versionSlug: PropTypes.string.isRequired,
        localeId: PropTypes.string.isRequired,
        localeDisplayName: PropTypes.string.isRequired,
        savedState: PropTypes.string.isRequired,
        wordCount: PropTypes.number.isRequired
      })
  ).isRequired,
  fromDate: PropTypes.string.isRequired,
  toDate: PropTypes.string.isRequired,
  dateRangeOption: PropTypes.oneOf(DateRanges).isRequired,
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  selectedDay: PropTypes.string,
  handleFilterChanged: PropTypes.func,
  handleSelectedDayChanged: PropTypes.func
}

export default FilterableMatrixTable
