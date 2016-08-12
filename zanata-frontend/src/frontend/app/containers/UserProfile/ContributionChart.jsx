import React from 'react'
import utilsDate from '../../utils/DateHelper'
import { Line as LineChart } from 'react-chartjs'

var defaultChartOptions = {
  animationEasing: 'easeOutQuint',
  bezierCurve: true,
  bezierCurveTension: 0.4,
  pointDot: true,
  pointDotRadius: 4,
  // This doesn't seem to work
  datasetStroke: true,
  datasetStrokeWidth: 2,
  datasetFill: true,
  responsive: true,
  showTooltips: true,
  scaleFontFamily: '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, ' +
    'Helvetica, Arial, sans-serif',
  scaleFontColor: 'rgb(84, 102, 122)',
  scaleShowGridLines: true,
  scaleShowVerticalLines: false,
  scaleGridLineColor: 'rgba(198, 210, 219, .1)',
  tooltipFillColor: 'rgba(255,255,255,0.8)',
  // scaleOverride : true,
  // scaleSteps : 10,
  // scaleStepWidth : 100,
  // scaleStartValue : 0,
  tooltipFontFamily: '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, ' +
    'Helvetica, Arial, sans-serif',
  tooltipFontSize: 14,
  tooltipFontStyle: '400',
  tooltipFontColor: 'rgb(132, 168, 196)',
  tooltipTitleFontFamily: '"Source Sans Pro", "Helvetica Neue", ' +
    'HelveticaNeue, Helvetica, Arial, sans-serif',
  tooltipTitleFontSize: 14,
  tooltipTitleFontStyle: '400',
  tooltipTitleFontColor: 'rgb(84, 102, 122)',
  tooltipYPadding: 6,
  tooltipXPadding: 6,
  tooltipCaretSize: 6,
  tooltipCornerRadius: 2,
  tooltipXOffset: 10,
  multiTooltipTemplate: '<%= value %><%if (datasetLabel)' +
    '{%> (<%= datasetLabel %>)<%}%>'
}

function convertMatrixDataToChartData (matrixData) {
  var chartData = {
    labels: [],
    datasets: [
      {
        label: 'Total',
        fillColor: 'rgba(84, 102, 122, .05)',
        strokeColor: 'rgb(84, 102, 122)',
        pointColor: 'rgb(84, 102, 122)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(84, 102, 122)',
        data: []
      },
      {
        label: 'Translated',
        fillColor: 'rgba(36, 200, 137, .05)',
        strokeColor: 'rgb(36, 200, 137)',
        pointColor: 'rgb(36, 200, 137)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(36, 200, 137)',
        data: []
      },
      {
        label: 'Needs Work',
        fillColor: 'rgba(235, 236, 21, .05)',
        strokeColor: 'rgb(235, 236, 21)',
        pointColor: 'rgb(235, 236, 21)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(235, 236, 21)',
        data: []
      },
      {
        label: 'Approved',
        fillColor: 'rgba(27, 167, 217, .05)',
        strokeColor: 'rgb(27, 167, 217)',
        pointColor: 'rgb(27, 167, 217)',
        pointStrokeColor: '#fff',
        pointHighlightFill: '#fff',
        pointHighlightStroke: 'rgb(27, 167, 217)',
        data: []
      }
    ]
  }
  var numOfDays = matrixData.length
  var intoTheFuture = false

  matrixData.forEach(function (value) {
    var date = value['date']
    intoTheFuture = intoTheFuture || utilsDate.isInFuture(date)
    chartData.labels.push(utilsDate.dayAsLabel(date, numOfDays))

    if (!intoTheFuture) {
      chartData['datasets'][0]['data'].push(value['totalActivity'])
      chartData['datasets'][1]['data'].push(value['totalTranslated'])
      chartData['datasets'][2]['data'].push(value['totalNeedsWork'])
      chartData['datasets'][3]['data'].push(value['totalApproved'])
    }
  })
  return chartData
}

var ContributionChart = React.createClass({
  propTypes: {
    dateRangeOption: React.PropTypes.object.isRequired,
    wordCountForEachDay: React.PropTypes.arrayOf(
      React.PropTypes.shape(
        {
          date: React.PropTypes.string.isRequired,
          totalActivity: React.PropTypes.number.isRequired,
          totalApproved: React.PropTypes.number.isRequired,
          totalTranslated: React.PropTypes.number.isRequired,
          totalNeedsWork: React.PropTypes.number.isRequired
        })
    ).isRequired,
    chartOptions: React.PropTypes.object
  },

  getDefaultProps: function () {
    return {
      chartOptions: defaultChartOptions
    }
  },

  shouldComponentUpdate: function (nextProps, nextState) {
    return this.props.dateRangeOption !== nextProps.dateRangeOption ||
      this.props.wordCountForEachDay.length !==
      nextProps.wordCountForEachDay.length
  },

  render: function () {
    const {
      wordCountForEachDay,
      chartOptions
    } = this.props
    var chartData = convertMatrixDataToChartData(wordCountForEachDay)
    return (
      <LineChart
        data={chartData}
        options={chartOptions}
        width='800'
        height='250' />
    )
  }
})

export default ContributionChart
