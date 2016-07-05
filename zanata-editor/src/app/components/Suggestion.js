import React, { PropTypes } from 'react'
import SuggestionSources from './SuggestionSources'
import SuggestionTranslations from './SuggestionTranslations'

/**
 * Display a single suggestion source, translation and metadata.
 */
const Suggestion = React.createClass({

  propTypes: {
    // true when the translation has just been copied
    suggestion: PropTypes.shape({
      copying: PropTypes.bool.isRequired,
      copySuggestion: PropTypes.func.isRequired,
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
    showDiff: PropTypes.bool.isRequired
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
      suggestion
    }
    return (
      <div
        className={className}>
        <div className="TransUnit-status"/>
        <SuggestionSources {...props}/>
        <SuggestionTranslations
          suggestion={suggestion}/>
      </div>
    )
  }
})

export default Suggestion
