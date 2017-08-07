import React from 'react'
import PropTypes from 'prop-types'
import utilsDate from '../../utils/DateHelper'
import { Line as LineChart } from 'react-chartjs-2'

const defaultFonts = '"Source Sans Pro", "Helvetica Neue", HelveticaNeue, ' +
  'Helvetica, Arial, sans-serif'

const defaultChartOptions = {
  animation: {
    easing: 'easeOutQuint'
  },
  legend: {
    display: false
  },
  elements: {
    line: {
      // curviness
      tension: 0.4,
      // thickness of line
      borderWidth: 2
    },
    point: {
      radius: 3,
      hoverRadius: 4,
      hitRadius: 10,
      borderWidth: 0,
      borderColor: '#fff'
    }
  },
  responsive: true,
  scales: {
    xAxes: [{
      ticks: {
        fontColor: 'rgb(84, 102, 122)',
        fontFamily: defaultFonts
      },
      gridLines: {
        drawOnChartArea: false
      }
    }],
    yAxes: [{
      ticks: {
        fontColor: 'rgb(84, 102, 122)',
        fontFamily: defaultFonts,
        // chart should always start at 0, someone can't have negative contrib.
        min: 0,
        suggestedMax: 10
      },
      gridLines: {
        color: 'rgba(198, 210, 219, .1)',
        drawBorder: true
      }
    }]
  },
  tooltips: {
    mode: 'index',
    intersect: false,
    backgroundColor: 'rgba(255,255,255,0.92)',
    borderColor: '#ccc',
    borderWidth: 1,
    yPadding: 8,
    xPadding: 6,
    caretSize: 6,
    cornerRadius: 2,
    titleFontFamily: defaultFonts,
    titleFontSize: 14,
    titleFontStyle: '400',
    titleFontColor: 'rgb(84, 102, 122)',
    titleSpacing: 0,
    titleMarginBottom: 12,
    bodyFontFamily: defaultFonts,
    bodyFontSize: 14,
    bodyFontStyle: '400',
    bodyFontColor: 'rgb(132, 168, 196)',
    bodySpacing: 6,
    // http://www.chartjs.org/docs/latest/configuration/tooltip.html
    callbacks: {
      label: ({ datasetIndex, index }, { datasets }) => {
        const dataset = datasets[datasetIndex]
        const { data, label } = dataset
        const val = data[index]
        return ` ${val} (${label})`
      }
    }
  }
}

function convertMatrixDataToChartData (matrixData) {
  var chartData = {
    labels: [],
    datasets: [
      {
        label: 'Approved',
        // fill colour under the line
        backgroundColor: 'rgba(27, 167, 217, .05)',
        // colour of the line
        borderColor: 'rgb(27, 167, 217)',
        pointBackgroundColor: 'rgb(27, 167, 217)',
        pointHighlightStroke: 'rgb(27, 167, 217)',
        data: []
      },
      {
        label: 'Needs Work',
        // fill colour under the line
        backgroundColor: 'rgba(235, 236, 21, .05)',
        // colour of the line
        borderColor: 'rgb(235, 236, 21)',
        pointBackgroundColor: 'rgb(235, 236, 21)',
        pointHighlightStroke: 'rgb(235, 236, 21)',
        data: []
      },
      {
        label: 'Translated',
        // fill colour under the line
        backgroundColor: 'rgba(36, 200, 137, .05)',
        // colour of the line
        borderColor: 'rgb(36, 200, 137)',
        pointBackgroundColor: 'rgb(36, 200, 137)',
        pointHighlightStroke: 'rgb(36, 200, 137)',
        data: []
      },
      {
        label: 'Total',
        // fill colour under the line
        backgroundColor: 'rgba(84, 102, 122, .05)',
        // colour of the line
        borderColor: 'rgb(84, 102, 122)',
        pointBackgroundColor: 'rgb(84, 102, 122)',
        pointHighlightStroke: 'rgb(84, 102, 122)',
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
      chartData['datasets'][0]['data'].push(value['totalApproved'])
      chartData['datasets'][1]['data'].push(value['totalNeedsWork'])
      chartData['datasets'][2]['data'].push(value['totalTranslated'])
      chartData['datasets'][3]['data'].push(value['totalActivity'])
    }
  })

  return chartData
}

class ContributionChart extends React.Component {
  static propTypes = {
    dateRange: PropTypes.object.isRequired,
    wordCountForEachDay: PropTypes.arrayOf(
      PropTypes.shape(
        {
          date: PropTypes.string.isRequired,
          totalActivity: PropTypes.number.isRequired,
          totalApproved: PropTypes.number.isRequired,
          totalTranslated: PropTypes.number.isRequired,
          totalNeedsWork: PropTypes.number.isRequired
        })
    ).isRequired,
    chartOptions: PropTypes.object
  }

  static defaultProps = {
    chartOptions: defaultChartOptions
  }

  shouldComponentUpdate (nextProps, nextState) {
    return this.props.dateRange !== nextProps.dateRange ||
      this.props.wordCountForEachDay.length !==
      nextProps.wordCountForEachDay.length
  }

  render () {
    const {
      wordCountForEachDay,
      chartOptions
    } = this.props
    var chartData = convertMatrixDataToChartData(wordCountForEachDay)
    return (
      <LineChart
        data={chartData}
        options={chartOptions}
        width={800}
        height={250} />
    )
  }
}

export default ContributionChart
