import React from 'react'
import PropTypes from 'prop-types'
import SuggestionContents from '../SuggestionContents'
import SuggestionDetailsSummary from '../SuggestionDetailsSummary'

/**
 * Display all the source strings for a suggestion, with
 * optional diff against a set of search strings.
 */
class SuggestionSources extends React.Component {
  static propTypes = {
    suggestion: PropTypes.shape({
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.string.isRequired,
        contentState: PropTypes.string
      })),
      sourceContents: PropTypes.arrayOf(PropTypes.string).isRequired
    }),
    search: PropTypes.arrayOf(PropTypes.string),
    showDiff: PropTypes.bool.isRequired,
    showDetail: PropTypes.func.isRequired,
    directionClass: PropTypes.string
  }

  render () {
    const sourceContents = this.props.suggestion.sourceContents
    return (
      <div className={this.props.directionClass +
        ' TransUnit-panel TransUnit-source'}>
        <SuggestionContents
          plural={sourceContents.length > 1}
          contents={sourceContents}
          showDiff={this.props.showDiff}
          compareTo={this.props.search} />
        <SuggestionDetailsSummary
          onClick={this.props.showDetail}
          suggestion={this.props.suggestion} />
      </div>
    )
  }
}

export default SuggestionSources
