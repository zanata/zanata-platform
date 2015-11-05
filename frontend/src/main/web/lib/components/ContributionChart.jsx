import React from 'react';
import utilsDate from '../utils/DateHelper';
import { Line } from 'react-chartjs';
import {DateRanges} from '../constants/Options';

var LineChart = Line;

var defaultChartOptions = {
  animationEasing: "easeOutQuint",
  bezierCurve : true,
  bezierCurveTension : 0.4,
  pointDot : true,
  pointDotRadius : 4,
  // This doesn't seem to work
  datasetStroke : true,
  datasetStrokeWidth : 2,
  datasetFill : true,
  // TODO: Need to set this to true but it breaks
  responsive: true,
  showTooltips: true,
  scaleFontFamily: '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, Helvetica, Arial, sans-serif',
  scaleFontColor: "#7c96ac",
  scaleShowGridLines : true,
  scaleShowVerticalLines: false,
  scaleGridLineColor : "rgba(198, 210, 219, .1)",
  tooltipFillColor: "rgba(255,255,255,0.8)",
  // scaleOverride : true,
  // scaleSteps : 10,
  // scaleStepWidth : 100,
  // scaleStartValue : 0,
  tooltipFontFamily: '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, Helvetica, Arial, sans-serif',
  tooltipFontSize: 14,
  tooltipFontStyle: '400',
  tooltipFontColor: 'rgb(132, 168, 196)',
  tooltipTitleFontFamily: '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, Helvetica, Arial, sans-serif',
  tooltipTitleFontSize: 14,
  tooltipTitleFontStyle: '400',
  tooltipTitleFontColor: 'rgb(65, 105, 136)',
  tooltipYPadding: 6,
  tooltipXPadding: 6,
  tooltipCaretSize: 6,
  tooltipCornerRadius: 2,
  tooltipXOffset: 10,
  multiTooltipTemplate: "<%= value %><%if (datasetLabel){%> (<%= datasetLabel %>)<%}%>"
};

function convertMatrixDataToChartData(matrixData) {
  var chartData = {
    labels: [],
    datasets: [
      {
        label: 'Total',
        fillColor: 'rgba(65, 105, 136, .05)',
        strokeColor: 'rgb(65, 105, 136)',
        pointColor: 'rgb(65, 105, 136)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(65, 105, 136)',
        data: []
      },
      {
        label: 'Translated',
        fillColor: 'rgba(112,169,139, .05)',
        strokeColor: 'rgb(112,169,139)',
        pointColor: 'rgb(112,169,139)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(112,169,139)',
        data: []
      },
      {
        label: 'Needs Work',
        fillColor: 'rgba(224,195,80, .05)',
        strokeColor: 'rgb(224,195,80)',
        pointColor: 'rgb(224,195,80)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(224,195,80)',
        data: []
      },
      {
        label: 'Approved',
        fillColor: "rgba(78, 159, 221, .05)",
        strokeColor: 'rgb(78, 159, 221)',
        pointColor: 'rgb(78, 159, 221)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(78, 159, 221)',
        data: []
      }
    ]
  },
    numOfDays = matrixData.length,
    intoTheFuture = false;


  matrixData.forEach(function(value) {
    var date = value['date'];
    intoTheFuture = intoTheFuture || utilsDate.isInFuture(date);
    chartData.labels.push(utilsDate.dayAsLabel(date, numOfDays));

    if (!intoTheFuture) {
      chartData['datasets'][0]['data'].push(value['totalActivity']);
      chartData['datasets'][1]['data'].push(value['totalTranslated']);
      chartData['datasets'][2]['data'].push(value['totalNeedsWork']);
      chartData['datasets'][3]['data'].push(value['totalApproved']);
    }
  });

  return chartData;
}

var ContributionChart = React.createClass({
  propTypes: {
    dateRangeOption: React.PropTypes.oneOf(DateRanges).isRequired,
    wordCountForEachDay: React.PropTypes.arrayOf(
      React.PropTypes.shape(
        {
          date: React.PropTypes.string.isRequired,
          totalActivity: React.PropTypes.number.isRequired,
          totalApproved: React.PropTypes.number.isRequired,
          totalTranslated: React.PropTypes.number.isRequired,
          totalNeedsWork: React.PropTypes.number.isRequired
        })
    ).isRequired
  },

  getDefaultProps: function() {
    return {
      chartOptions: defaultChartOptions
    }
  },

  shouldComponentUpdate: function(nextProps, nextState) {
    return this.props.dateRangeOption !== nextProps.dateRangeOption
      || this.props.wordCountForEachDay.length !== nextProps.wordCountForEachDay.length;
  },

  render: function() {
    var chartData = convertMatrixDataToChartData(this.props.wordCountForEachDay);
    return (
      <LineChart data={chartData} options={this.props.chartOptions} width="800" height="250"/>
    )
  }
});

export default ContributionChart;
