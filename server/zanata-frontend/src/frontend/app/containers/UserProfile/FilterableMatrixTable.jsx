import React, { PropTypes } from 'react'
import ContentStateFilter from './ContentStateFilter'
import CalendarMonthMatrix from './CalendarMonthMatrix'
import CalendarPeriodHeading from './CalendarPeriodHeading'
import CategoryMatrixTable from './CategoryMatrixTable'
import {
  ContentStates
} from '../../constants/Options'
/**
 * Bottom section of contribution statistic page.
 * See RecentContribution for main page.
 */
const FilterableMatrixTable = ({
  dateRange,
  selectedContentState,
  selectedDay,
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
    : <div className='text-muted'>No contributions</div>
  return (
    <div>
      <ContentStateFilter selectedContentState={selectedContentState}
        handleFilterChanged={handleFilterChanged} />
      <div className='matrix-table'>
        <div className='matrix-inner'>
          <CalendarMonthMatrix
            matrixData={wordCountForEachDay}
            selectedDay={selectedDay}
            selectedContentState={selectedContentState}
            dateRange={dateRange}
            handleSelectedDayChanged={handleSelectedDayChanged} />
        </div>
        <div className='matrix-heading'>
          <CalendarPeriodHeading
            dateRange={dateRange}
            selectedDay={selectedDay} />
          {categoryTables}
        </div>
      </div>
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
  dateRange: PropTypes.object.isRequired,
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  selectedDay: PropTypes.string,
  handleFilterChanged: PropTypes.func,
  handleSelectedDayChanged: PropTypes.func
}

export default FilterableMatrixTable
