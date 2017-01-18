import React, { PropTypes } from 'react'
import SuggestionSources from './SuggestionSources'
import SuggestionTranslations from './SuggestionTranslations'

/**
 * Display a single suggestion source, translation and metadata.
 */
const Suggestion = React.createClass({

  propTypes: {
    copySuggestion: PropTypes.func.isRequired,
    index: PropTypes.number.isRequired,
    suggestion: PropTypes.shape({
      // true when the translation has just been copied
      copying: PropTypes.bool.isRequired,
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.oneOf(
          ['IMPORTED_TM', 'LOCAL_PROJECT']).isRequired,
        contentState: PropTypes.oneOf(['Translated', 'Approved'])
      })),
      similarityPercent: PropTypes.number,
      sourceContents: PropTypes.arrayOf(
        PropTypes.string).isRequired,
      targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
    }),
    search: PropTypes.arrayOf(PropTypes.string),
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired
  },

  /**
   * Calculate the match type for the suggestion
   */
  matchType: function (suggestion) {
    let topMatch = suggestion.matchDetails[0]

    if (topMatch.type === 'IMPORTED_TM') {
      return 'imported'
    }
    if (topMatch.type === 'LOCAL_PROJECT') {
      if (topMatch.contentState === 'Translated') {
        return 'translated'
      }
      if (topMatch.contentState === 'Approved') {
        return 'approved'
      }
    }
    console.error('Unable to generate row display type for top match')
  },

  matchTypeClass: {
    imported: 'TransUnit--secondary',
    translated: 'TransUnit--success',
    approved: 'TransUnit--highlight'
  },

  copySuggestion () {
    this.props.copySuggestion(this.props.index)
  },

  showDetail () {
    this.props.showDetail(this.props.index)
  },

  render: function () {
    const matchType = this.matchType(this.props.suggestion)
    const className = 'TransUnit TransUnit--suggestion ' +
                        this.matchTypeClass[matchType]
    const suggestion = {
      ...this.props.suggestion,
      matchType
    }
    const props = {
      ...this.props,
      suggestion,
      showDetail: this.showDetail
    }
    return (
      <div
        className={className}>
        <div className="TransUnit-status" />
        <SuggestionSources {...props} />
        <SuggestionTranslations
          copySuggestion={this.copySuggestion}
          suggestion={suggestion} />
      </div>
    )
  }
})

export default Suggestion
