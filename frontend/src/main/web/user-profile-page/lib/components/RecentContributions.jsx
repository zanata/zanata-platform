import React from 'react';
import ContributionChart from './ContributionChart';
import DropDown from './DropDown';
import FilterableMatrixTable from './FilterableMatrixTable';
import UserMatrixStore from '../stores/UserMatrixStore';
import {DateRanges} from '../constants/Options';

var RecentContributions = React.createClass(
  {

    getMatrixState: function() {
      return UserMatrixStore.getMatrixState();
    },

    getInitialState: function() {
      return this.getMatrixState();
    },

    componentDidMount: function() {
      UserMatrixStore.addChangeListener(this._onChange);
    },

    componentWillUnmount: function() {
      UserMatrixStore.removeChangeListener(this._onChange);
    },

    _onChange: function() {
      this.setState(this.getMatrixState());
    },

    render: function() {
      var dateRange = this.state.dateRange;

      var loader = (
        <a href="#" className="loader--large is-active">
          <span className="loader__spinner">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </a>);
      var chart = this.state.loading ? loader : (
        <ContributionChart wordCountForEachDay={this.state.matrixForAllDays}
          dateRangeOption={this.state.dateRangeOption} />);
      var matrix = this.state.loading ? undefined : (
        <FilterableMatrixTable
          wordCountForSelectedDay={this.state.wordCountsForSelectedDayFilteredByContentState}
          wordCountForEachDay={this.state.wordCountsForEachDayFilteredByContentState}
          fromDate={dateRange['fromDate']} toDate={dateRange['toDate']}
          dateRangeOption={this.state.dateRangeOption}
          selectedContentState={this.state.contentStateOption}
          selectedDay={this.state.selectedDay}
          />);

      return (
        <div className="l__wrapper">
          <div className="l--push-bottom-1">
            <div className="l--float-right txt--uppercase">
              <DropDown options={DateRanges} selectedOption={this.state.dateRangeOption}  />
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
