import React, { PropTypes } from 'react'
import SuggestionContents from './SuggestionContents'
import SuggestionDetailsSummary from './SuggestionDetailsSummary'

/**
 * Display all the source strings for a suggestion, with
 * optional diff against a set of search strings.
 */
const SuggestionSources = React.createClass({
  propTypes: {
    suggestion: PropTypes.shape({
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.string.isRequired,
        contentState: PropTypes.string
      })),
      sourceContents: PropTypes.arrayOf(PropTypes.string).isRequired
    }),
    search: PropTypes.arrayOf(PropTypes.string),
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired
  },

  render: function () {
    const sourceContents = this.props.suggestion.sourceContents
    const diffWith = this.props.showDiff ? this.props.search : undefined
    return (
      <div className="TransUnit-panel TransUnit-source">
        <SuggestionContents
          plural={sourceContents.length > 1}
          contents={sourceContents}
          compareTo={diffWith} />
        <SuggestionDetailsSummary
          onClick={this.props.showDetail}
          suggestion={this.props.suggestion} />
      </div>
    )
  }
})

export default SuggestionSources
