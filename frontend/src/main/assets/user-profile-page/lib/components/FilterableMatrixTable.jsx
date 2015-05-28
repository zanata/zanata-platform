import React from 'react/addons';
import ContentStateFilter from './ContentStateFilter';
import CalendarMonthMatrix from './CalendarMonthMatrix'
import CalendarPeriodHeading from './CalendarPeriodHeading';
import CategoryMatrixTable from './CategoryMatrixTable';
import {DateRanges} from '../constants/Options';
import {ContentStates} from '../constants/Options';

var FilterableMatrixTable = React.createClass({
  propTypes: {
    wordCountForEachDay: React.PropTypes.arrayOf(
      React.PropTypes.shape(
        {
          date: React.PropTypes.string.isRequired,
          wordCount: React.PropTypes.number.isRequired,
        })
    ).isRequired,
    wordCountForSelectedDay: React.PropTypes.arrayOf(
      React.PropTypes.shape(
        {
          savedDate: React.PropTypes.string.isRequired,
          projectSlug: React.PropTypes.string.isRequired,
          projectName: React.PropTypes.string.isRequired,
          versionSlug: React.PropTypes.string.isRequired,
          localeId: React.PropTypes.string.isRequired,
          localeDisplayName: React.PropTypes.string.isRequired,
          savedState: React.PropTypes.string.isRequired,
          wordCount: React.PropTypes.number.isRequired
        })
    ).isRequired,
    fromDate: React.PropTypes.string.isRequired,
    toDate: React.PropTypes.string.isRequired,
    dateRangeOption: React.PropTypes.oneOf(DateRanges).isRequired,
    selectedContentState: React.PropTypes.oneOf(ContentStates).isRequired,
    selectedDay: React.PropTypes.string
  },

  render: function () {
    var selectedContentState = this.props.selectedContentState,
      selectedDay = this.props.selectedDay,
      categoryTables;

    if (this.props.wordCountForSelectedDay.length > 0) {
      categoryTables =
        [
          <CategoryMatrixTable key='locales' matrixData={this.props.wordCountForSelectedDay} category='localeId' categoryTitle='localeDisplayName' categoryName='Languages' />,
          <CategoryMatrixTable key='projects' matrixData={this.props.wordCountForSelectedDay} category='projectSlug' categoryTitle='projectName' categoryName='Projects' />
        ];
    } else {
      categoryTables = <div>No contributions</div>
    }
    return (
      <div>
        <ContentStateFilter selectedContentState={selectedContentState}  />
        <div className="g">
          <div className="g__item w--1-2-l w--1-2-h">
            <CalendarMonthMatrix matrixData={this.props.wordCountForEachDay} selectedDay={selectedDay} selectedContentState={selectedContentState} dateRangeOption={this.props.dateRangeOption} />
          </div>
          <div className="g__item w--1-2-l w--1-2-h">
            <CalendarPeriodHeading fromDate={this.props.fromDate} toDate={this.props.toDate} dateRange={this.props.dateRangeOption} selectedDay={selectedDay}/>
            {categoryTables}
          </div>
        </div>
      </div>
    )
  }
});

export default FilterableMatrixTable;
