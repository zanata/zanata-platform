import React, { PropTypes } from 'react'
import SuggestionContents from './SuggestionContents'
import SuggestionTranslationDetails from './SuggestionTranslationDetails'

/**
 * Show all translations for a suggestion, with translation metadata.
 */
const SuggestionTranslations = React.createClass({
  propTypes: {
    copySuggestion: PropTypes.func.isRequired,
    suggestion: PropTypes.shape({
      copying: PropTypes.bool.isRequired,
      matchType: PropTypes.string.isRequired,
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.string.isRequired,
        contentState: PropTypes.string
      })),
      similarityPercent: PropTypes.number,
      sourceContents: PropTypes.arrayOf(
        PropTypes.string).isRequired,
      targetContents: PropTypes.arrayOf(PropTypes.string).isRequired
    })
  },

  render: function () {
    return (
      <div className="TransUnit-panel TransUnit-translation u-sPV-1-2">
        <SuggestionContents
          plural={this.props.suggestion.sourceContents.length > 1}
          contents={this.props.suggestion.targetContents} />
        <SuggestionTranslationDetails {... this.props} />
      </div>
    )
  }
})

export default SuggestionTranslations
