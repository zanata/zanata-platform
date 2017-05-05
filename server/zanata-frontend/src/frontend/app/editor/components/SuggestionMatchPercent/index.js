import React, { PropTypes } from 'react'

/**
 * Show a percentage for a match, styled for the match type.
 */
const SuggestionMatchPercent = React.createClass({

  propTypes: {
    matchType: PropTypes.oneOf(['imported', 'translated', 'approved'])
      .isRequired,
    percent: PropTypes.number
  },

  percentageDisplayString: (percent) => {
    if (!isFinite(percent)) {
      return undefined
    }

    // Prevent very high percentages displaying as 100%
    if (percent > 99.99 && percent < 100) {
      return '99.99%'
    }
    // show 2 decimal places when near 100%
    if (percent >= 99.90 && percent < 100) {
      return percent.toFixed(2) + '%'
    }

    // Limit any inexact percentages to a single decimal place
    if (Math.round(percent) !== percent) {
      return percent.toFixed(1) + '%'
    }

    return percent.toString() + '%'
  },

  displayClass: {
    imported: 'u-textSecondary',
    translated: 'u-textSuccess',
    approved: 'u-textHighlight'
  },

  render: function () {
    return (
      <div className={this.displayClass[this.props.matchType]}>
        {this.percentageDisplayString(this.props.percent)}
      </div>
    )
  }
})

export default SuggestionMatchPercent
