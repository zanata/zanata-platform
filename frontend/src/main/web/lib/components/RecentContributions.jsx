import React from 'react';
import ContributionChart from './ContributionChart';
import DropDown from './DropDown';
import FilterableMatrixTable from './FilterableMatrixTable';
import {DateRanges} from '../constants/Options';
import Actions from '../actions/UserMatrixActions';

var RecentContributions = React.createClass(
  {
    render: function() {
      var dateRange = this.props.dateRange,
        chart,
        matrix;

      if(this.props.loading) {
        chart = (
          <a href="#" className="loader--large is-active">
          <span className="loader__spinner">
            <span></span>
            <span></span>
            <span></span>
          </span>
          </a>);
      } else {
        chart = (
          <ContributionChart wordCountForEachDay={this.props.matrixForAllDays} dateRangeOption={this.props.dateRangeOption} />);

        matrix =  (
          <FilterableMatrixTable
            wordCountForSelectedDay={this.props.wordCountsForSelectedDayFilteredByContentState}
            wordCountForEachDay={this.props.wordCountsForEachDayFilteredByContentState}
            fromDate={dateRange.fromDate} toDate={dateRange.toDate}
            dateRangeOption={this.props.dateRangeOption}
            selectedContentState={this.props.contentStateOption}
            selectedDay={this.props.selectedDay}
            />);
      }
      return (
        <div className="l__wrapper">
          <div className="l--push-bottom-1">
            <div className="l--float-right txt--uppercase">
              <DropDown options={DateRanges} selectedOption={this.props.dateRangeOption} onSelectionChange={Actions.changeDateRange}/>
            </div>
            <h2 className='delta txt--uppercase'>Recent Contributions</h2>
          </div>
          <div className="l--push-bottom-1">
            {chart}
          </div>
          {matrix}
        </div>
      )
    }
  }
);

export default RecentContributions;
